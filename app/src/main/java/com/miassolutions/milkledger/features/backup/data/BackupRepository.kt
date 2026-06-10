package com.miassolutions.milkledger.features.backup.data

import android.os.Build
import androidx.room.withTransaction
import com.miassolutions.milkledger.BuildConfig
import com.miassolutions.milkledger.core.localdb.database.AppDatabase
import com.miassolutions.milkledger.features.backup.drive.DriveBackupFile
import com.miassolutions.milkledger.features.backup.drive.DriveBackupResult
import com.miassolutions.milkledger.features.backup.drive.GoogleDriveBackupDataSource
import com.miassolutions.milkledger.features.backup.mapper.toBackupDto
import com.miassolutions.milkledger.features.backup.mapper.toEntity
import com.miassolutions.milkledger.features.backup.model.BackupCounts
import com.miassolutions.milkledger.features.backup.model.BackupMetadata
import com.miassolutions.milkledger.features.backup.model.MilkLedgerBackup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.serializer
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupRepository @Inject constructor(
    private val database: AppDatabase,
    private val backupFileManager: BackupFileManager,
    private val backupValidator: BackupValidator,
    private val googleDriveBackupDataSource: GoogleDriveBackupDataSource,
    private val backupPrefs: BackupPrefs
) {
    
    suspend fun createLocalBackupBytes(): ByteArray {
        val backupWithoutChecksum = createBackupObject()
        
        val jsonWithoutChecksum = BackupJson.json.encodeToString(
            MilkLedgerBackup.serializer(),
            backupWithoutChecksum.copy(checksum = "")
        )
        
        val checksum = BackupChecksum.sha256(jsonWithoutChecksum)
        
        val backupWithChecksum = backupWithoutChecksum.copy(
            checksum = checksum
        )
        
        val finalJson = BackupJson.json.encodeToString(
            MilkLedgerBackup.serializer(),
            backupWithChecksum
        )
        
        return BackupCompressor.gzip(finalJson)
    }
    
    suspend fun createLocalBackupFile(): File {
        val backupBytes = createLocalBackupBytes()
        val now = System.currentTimeMillis()
        
        return backupFileManager.saveLocalBackupFile(
            bytes = backupBytes,
            createdAtMillis = now
        )
    }
    
    fun generateBackupFileNameForExport(): String {
        return backupFileManager.generateBackupFileName()
    }
    
    fun copyUriToTempBackupFile(
        uri: android.net.Uri,
        contentResolver: android.content.ContentResolver,
        cacheDir: File
    ): File {
        val tempFile = File(
            cacheDir,
            "restore_temp_${System.currentTimeMillis()}.mlbackup"
        )
        
        contentResolver.openInputStream(uri)?.use { input ->
            tempFile.outputStream().use { output ->
                input.copyTo(output)
            }
        } ?: error("Unable to open selected backup file")
        
        return tempFile
    }
    
    suspend fun restoreFromDriveBackup(file: DriveBackupFile) {
        val downloadedFile = downloadBackupFromDrive(file)
        
        restoreFromLocalBackupFile(downloadedFile)
    }
    
    suspend fun listDriveBackups(): List<DriveBackupFile> {
        return withContext(Dispatchers.IO) {
            googleDriveBackupDataSource.listBackupFiles()
        }
    }
    
    suspend fun downloadBackupFromDrive(file: DriveBackupFile): File {
        return withContext(Dispatchers.IO) {
            val destination = backupFileManager.createDownloadedBackupFile(file.name)
            
            googleDriveBackupDataSource.downloadBackupFile(
                fileId = file.fileId,
                destinationFile = destination
            )
        }
    }
    
  

    
    fun markDataChanged() {
        backupPrefs.markDataChanged()
    }
    
    suspend fun createBackupObject(): MilkLedgerBackup {
        val accounts = database.accountDao().getAllAccountsForBackup().map { it.toBackupDto() }
        val milkTransactions =
            database.milkDao().getAllMilkTransactionsForBackup().map { it.toBackupDto() }
        val ledgerEntries =
            database.ledgerDao().getAllLedgerEntriesForBackup().map { it.toBackupDto() }
        val expenses = database.expenseDao().getAllExpensesForBackup().map { it.toBackupDto() }
        val notes = database.noteDao().getAllNotesForBackup().map { it.toBackupDto() }
        
        val counts = BackupCounts(
            accounts = accounts.size,
            milkTransactions = milkTransactions.size,
            ledgerEntries = ledgerEntries.size,
            expenses = expenses.size,
            notes = notes.size
        )
        
        val metadata = BackupMetadata(
            backupId = UUID.randomUUID().toString(),
            databaseVersion = 2, // Your current AppDatabase version is 2
            appVersionName = BuildConfig.VERSION_NAME,
            appVersionCode = BuildConfig.VERSION_CODE.toLong(),
            createdAtMillis = System.currentTimeMillis(),
            deviceName = getDeviceName()
        )
        
        return MilkLedgerBackup(
            metadata = metadata,
            counts = counts,
            accounts = accounts,
            milkTransactions = milkTransactions,
            ledgerEntries = ledgerEntries,
            expenses = expenses,
            notes = notes,
            checksum = ""
        )
    }
    
   
    
    fun readBackupFromFile(file: File): MilkLedgerBackup {
        val compressedBytes = file.readBytes()
        val json = BackupCompressor.ungzip(compressedBytes)
        
        return BackupJson.json.decodeFromString(
            MilkLedgerBackup.serializer(),
            json
        )
    }
    
    suspend fun restoreFromLocalBackupFile(file: File) {
        val backup = readBackupFromFile(file)
        
        backupValidator.validateBeforeRestore(backup)
        
        // Emergency local backup before replacing current DB
        createLocalBackupFile()
        
        val accounts = backup.accounts.map { it.toEntity() }
        val milkTransactions = backup.milkTransactions.map { it.toEntity() }
        val ledgerEntries = backup.ledgerEntries.map { it.toEntity() }
        val expenses = backup.expenses.map { it.toEntity() }
        val notes = backup.notes.map { it.toEntity() }
        
        database.withTransaction {
            database.noteDao().clearNotesForRestore()
            database.ledgerDao().clearLedgerEntriesForRestore()
            database.milkDao().clearMilkTransactionsForRestore()
            database.expenseDao().clearExpensesForRestore()
            database.accountDao().clearAccountsForRestore()
            
            database.accountDao().insertAccountsFromBackup(accounts)
            database.milkDao().insertMilkTransactionsFromBackup(milkTransactions)
            database.ledgerDao().insertLedgerEntriesFromBackup(ledgerEntries)
            database.expenseDao().insertExpensesFromBackup(expenses)
            database.noteDao().insertNotesFromBackup(notes)
        }
        
        validateAfterRestore(backup)
    }
    
    private suspend fun validateAfterRestore(backup: MilkLedgerBackup) {
        val restoredAccounts = database.accountDao().getAllAccountsForBackup().size
        val restoredMilk = database.milkDao().getAllMilkTransactionsForBackup().size
        val restoredLedger = database.ledgerDao().getAllLedgerEntriesForBackup().size
        val restoredExpenses = database.expenseDao().getAllExpensesForBackup().size
        val restoredNotes = database.noteDao().getAllNotesForBackup().size
        
        require(restoredAccounts == backup.counts.accounts) {
            "Restore failed: accounts count mismatch"
        }
        
        require(restoredMilk == backup.counts.milkTransactions) {
            "Restore failed: milk transactions count mismatch"
        }
        
        require(restoredLedger == backup.counts.ledgerEntries) {
            "Restore failed: ledger entries count mismatch"
        }
        
        require(restoredExpenses == backup.counts.expenses) {
            "Restore failed: expenses count mismatch"
        }
        
        require(restoredNotes == backup.counts.notes) {
            "Restore failed: notes count mismatch"
        }
    }
    
    suspend fun createAndUploadBackupToDrive(): DriveBackupResult {
        val localBackupFile = createLocalBackupFile()
        
        return withContext(Dispatchers.IO) {
            val result = googleDriveBackupDataSource.uploadBackupFile(localBackupFile)
            
            backupPrefs.markBackupSuccess(
                timeMillis = System.currentTimeMillis(),
                fileName = result.fileName
            )
            
            // Cleanup should happen only after successful upload.
            try {
                googleDriveBackupDataSource.deleteOldBackupsKeepingLatest(maxToKeep = 10)
            } catch (e: Exception) {
                android.util.Log.e("MilkBackup", "Drive retention cleanup failed", e)
            }
            
            result
        }
    }

    
    private fun getDeviceName(): String {
        val manufacturer = Build.MANUFACTURER.orEmpty()
        val model = Build.MODEL.orEmpty()
        
        return if (model.startsWith(manufacturer, ignoreCase = true)) {
            model
        } else {
            "$manufacturer $model"
        }.trim()
    }
}
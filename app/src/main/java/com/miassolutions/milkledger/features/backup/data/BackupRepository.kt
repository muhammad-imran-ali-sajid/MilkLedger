package com.miassolutions.milkledger.features.backup.data

import android.os.Build
import com.miassolutions.milkledger.BuildConfig
import com.miassolutions.milkledger.core.localdb.AppDatabase
import com.miassolutions.milkledger.features.backup.mapper.toBackupDto
import com.miassolutions.milkledger.features.backup.model.BackupCounts
import com.miassolutions.milkledger.features.backup.model.BackupMetadata
import com.miassolutions.milkledger.features.backup.model.MilkLedgerBackup
import kotlinx.serialization.builtins.serializer
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupRepository @Inject constructor(
    private val database: AppDatabase,
    private val backupFileManager: BackupFileManager
) {
    
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
    
    suspend fun createLocalBackupFile(): File {
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
        
        val compressedBytes = BackupCompressor.gzip(finalJson)
        
        return backupFileManager.saveLocalBackupFile(
            bytes = compressedBytes,
            createdAtMillis = backupWithChecksum.metadata.createdAtMillis
        )
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
package com.miassolutions.milkledger.features.backup.data

import android.os.Build
import androidx.room.withTransaction
import com.miassolutions.milkledger.BuildConfig
import com.miassolutions.milkledger.core.localdb.AppDatabase
import com.miassolutions.milkledger.features.backup.mapper.toBackupDto
import com.miassolutions.milkledger.features.backup.mapper.toEntity
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
    private val backupFileManager: BackupFileManager,
    private val backupValidator: BackupValidator
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
        
        val accounts = backup.accounts.map { it.toEntity() }
        val milkTransactions = backup.milkTransactions.map { it.toEntity() }
        val ledgerEntries = backup.ledgerEntries.map { it.toEntity() }
        val expenses = backup.expenses.map { it.toEntity() }
        val notes = backup.notes.map { it.toEntity() }
        
        database.withTransaction {
            // Clear child/dependent tables first
            database.noteDao().clearNotesForRestore()
            database.ledgerDao().clearLedgerEntriesForRestore()
            database.milkDao().clearMilkTransactionsForRestore()
            database.expenseDao().clearExpensesForRestore()
            database.accountDao().clearAccountsForRestore()
            
            // Restore parent/base tables first
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
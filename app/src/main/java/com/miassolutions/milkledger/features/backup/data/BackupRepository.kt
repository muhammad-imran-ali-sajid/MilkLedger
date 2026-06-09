package com.miassolutions.milkledger.features.backup.data

import android.os.Build
import com.miassolutions.milkledger.BuildConfig
import com.miassolutions.milkledger.core.localdb.AppDatabase
import com.miassolutions.milkledger.features.backup.model.BackupCounts
import com.miassolutions.milkledger.features.backup.model.BackupMetadata
import com.miassolutions.milkledger.features.backup.model.MilkLedgerBackup
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupRepository @Inject constructor(
    private val database: AppDatabase
) {
    
    suspend fun createBackupObject(): MilkLedgerBackup {
        val accounts = database.accountDao().getAllAccountsForBackup()
        val milkTransactions = database.milkDao().getAllMilkTransactionsForBackup()
        val ledgerEntries = database.ledgerDao().getAllLedgerEntriesForBackup()
        val expenses = database.expenseDao().getAllExpensesForBackup()
        val notes = database.noteDao().getAllNotesForBackup()
        
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
package com.miassolutions.milkledger.features.backup.data

import com.miassolutions.milkledger.features.backup.model.MilkLedgerBackup
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupValidator @Inject constructor() {
    
    fun validateBeforeRestore(backup: MilkLedgerBackup) {
        validateCounts(backup)
        validateChecksum(backup)
        validateReferences(backup)
    }
    
    private fun validateCounts(backup: MilkLedgerBackup) {
        require(backup.counts.accounts == backup.accounts.size) {
            "Backup accounts count mismatch"
        }
        
        require(backup.counts.milkTransactions == backup.milkTransactions.size) {
            "Backup milk transactions count mismatch"
        }
        
        require(backup.counts.ledgerEntries == backup.ledgerEntries.size) {
            "Backup ledger entries count mismatch"
        }
        
        require(backup.counts.expenses == backup.expenses.size) {
            "Backup expenses count mismatch"
        }
        
        require(backup.counts.notes == backup.notes.size) {
            "Backup notes count mismatch"
        }
    }
    
    private fun validateChecksum(backup: MilkLedgerBackup) {
        val backupWithoutChecksum = backup.copy(checksum = "")
        
        val jsonWithoutChecksum = BackupJson.json.encodeToString(
            MilkLedgerBackup.serializer(),
            backupWithoutChecksum
        )
        
        val calculatedChecksum = BackupChecksum.sha256(jsonWithoutChecksum)
        
        require(calculatedChecksum == backup.checksum) {
            "Backup checksum mismatch. File may be corrupted or modified."
        }
    }
    
    private fun validateReferences(backup: MilkLedgerBackup) {
        val accountIds = backup.accounts.map { it.accountId }.toSet()
        
        val missingMilkAccounts = backup.milkTransactions
            .filter { it.deletedAtMillis == null }
            .filter { it.accountId !in accountIds }
        
        require(missingMilkAccounts.isEmpty()) {
            "Backup has milk transactions with missing accounts: ${missingMilkAccounts.size}"
        }
        
        val missingLedgerAccounts = backup.ledgerEntries
            .filter { it.deletedAtMillis == null }
            .filter { it.accountId !in accountIds }
        
        require(missingLedgerAccounts.isEmpty()) {
            "Backup has ledger entries with missing accounts: ${missingLedgerAccounts.size}"
        }
    }
}
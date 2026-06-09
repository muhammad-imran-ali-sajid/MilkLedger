package com.miassolutions.milkledger.features.backup.model


import com.miassolutions.milkledger.features.backup.model.dto.AccountBackupDto
import com.miassolutions.milkledger.features.backup.model.dto.ExpenseBackupDto
import com.miassolutions.milkledger.features.backup.model.dto.LedgerBackupDto
import com.miassolutions.milkledger.features.backup.model.dto.MilkTransactionBackupDto
import com.miassolutions.milkledger.features.backup.model.dto.NoteBackupDto
import kotlinx.serialization.Serializable

@Serializable
data class MilkLedgerBackup(
    val metadata: BackupMetadata,
    val counts: BackupCounts,
    
    val accounts: List<AccountBackupDto>,
    val milkTransactions: List<MilkTransactionBackupDto>,
    val ledgerEntries: List<LedgerBackupDto>,
    val expenses: List<ExpenseBackupDto>,
    val notes: List<NoteBackupDto>,
    
    val checksum: String = ""
)
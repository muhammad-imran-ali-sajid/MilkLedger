package com.miassolutions.milkledger.features.backup.model

import com.miassolutions.milkledger.core.localdb.account.local.AccountEntity
import com.miassolutions.milkledger.core.localdb.expense.ExpenseEntity
import com.miassolutions.milkledger.core.localdb.ledger.FinancialLedgerEntity
import com.miassolutions.milkledger.core.localdb.milk.MilkTransactionEntity
import com.miassolutions.milkledger.features.note.data.local.NoteEntity

data class MilkLedgerBackup(
    val metadata: BackupMetadata,
    val counts: BackupCounts,
    
    val accounts: List<AccountEntity>,
    val milkTransactions: List<MilkTransactionEntity>,
    val ledgerEntries: List<FinancialLedgerEntity>,
    val expenses: List<ExpenseEntity>,
    val notes: List<NoteEntity>,
    
    // Day 2 mein real checksum add karenge.
    val checksum: String = ""
)
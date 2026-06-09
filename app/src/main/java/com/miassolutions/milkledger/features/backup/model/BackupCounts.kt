package com.miassolutions.milkledger.features.backup.model

data class BackupCounts(
    val accounts: Int,
    val milkTransactions: Int,
    val ledgerEntries: Int,
    val expenses: Int,
    val notes: Int
)
package com.miassolutions.milkledger.features.backup.model

import kotlinx.serialization.Serializable

@Serializable
data class BackupCounts(
    val accounts: Int,
    val milkTransactions: Int,
    val ledgerEntries: Int,
    val expenses: Int,
    val notes: Int
)
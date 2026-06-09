package com.miassolutions.milkledger.features.backup.model.dto

import kotlinx.serialization.Serializable

@Serializable
data class LedgerBackupDto(
    val ledgerId: String,
    val dateMillis: Long,
    val accountId: String,
    val referenceId: String?,
    val type: String,
    
    val debit: Long,
    val credit: Long,
    val profitImpact: Long,
    val note: String?,
    
    val createdAtMillis: Long,
    val updatedAtMillis: Long,
    val isSynced: Boolean,
    val deletedAtMillis: Long?
)
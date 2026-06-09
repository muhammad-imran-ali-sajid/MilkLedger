package com.miassolutions.milkledger.features.backup.model.dto

import kotlinx.serialization.Serializable

@Serializable
data class ExpenseBackupDto(
    val expenseId: String,
    val dateMillis: Long,
    val title: String,
    val amount: Long,
    val category: String?,
    val isPersonal: Boolean,
    val note: String?,
    
    val createdAtMillis: Long,
    val updatedAtMillis: Long,
    val isSynced: Boolean,
    val deletedAtMillis: Long?
)
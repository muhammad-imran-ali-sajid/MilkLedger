package com.miassolutions.milkledger.features.backup.model.dto

import kotlinx.serialization.Serializable

@Serializable
data class AccountBackupDto(
    val accountId: String,
    val name: String,
    val phone: String?,
    val accountType: String,
    val isActive: Boolean,
    val sortOrder: Int,
    val advanceAmount: Long?,
    val defaultRate: Double,
    val initialBalance: Long?,
    val createdAtMillis: Long,
    val updatedAtMillis: Long,
    val isSynced: Boolean,
    val deletedAtMillis: Long?
)




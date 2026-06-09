package com.miassolutions.milkledger.features.backup.model.dto

import kotlinx.serialization.Serializable

@Serializable
data class MilkTransactionBackupDto(
    val milkTransId: String,
    val accountId: String,
    val dateMillis: Long,
    val type: String,
    
    val volume: Double,
    val deduction: Double,
    val quantity: Double,
    
    val fat: Double?,
    val lr: Double?,
    val ts: Double?,
    
    val rateUsed: Double,
    val totalAmount: Long,
    val notes: String?,
    val paymentDateMillis: Long?,
    
    val createdAtMillis: Long,
    val updatedAtMillis: Long,
    val isSynced: Boolean,
    val deletedAtMillis: Long?
)
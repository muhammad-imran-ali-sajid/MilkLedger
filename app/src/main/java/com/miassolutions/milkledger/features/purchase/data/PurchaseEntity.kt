package com.miassolutions.milkledger.features.purchase.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "purchase_table",
    indices = [Index("supplierId"), Index("dateMillis")]
)
data class PurchaseEntity(
    @PrimaryKey val purchaseId: String = UUID.randomUUID().toString(),
    val supplierId: String,
    val dateMillis: Long,

    // Quality Details
    val milkAmount: Double,
    val fat: Double,
    val lr: Double,
    val ts: Double,

    // Rate Details
    val rateUsed: Double,
    val milkPrice: Double,
    val payment: Double,
    val balance: Double,

    val notes: String?,

    val createdAtMillis: Long = System.currentTimeMillis(),
    val updatedAtMillis: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false,
    val deletedAtMillis: Long? = null
)
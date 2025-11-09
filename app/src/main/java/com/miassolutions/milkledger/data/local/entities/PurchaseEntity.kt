package com.miassolutions.milkledger.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

@Entity(
    tableName = "purchase_table",
    foreignKeys = [
        ForeignKey(
            entity = SupplierEntity::class,
            parentColumns = ["supplierId"],
            childColumns = ["supplierId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("supplierId"), Index("date")]
)
data class PurchaseEntity(
    @PrimaryKey val purchaseId: String = UUID.randomUUID().toString(),
    val supplierId: String,
    val date: LocalDate,
    val milkAmount: Double,
    val fat: Double,
    val lr: Double,
    val ts: Double,
    val milkPrice: Double,
    val payment: Double,
    val balance: Double,
    val rateUsed: Double,
    val notes: String? = null,

    val isSynced: Boolean = false,
    val updatedAt: String = LocalDateTime.now().toString(),
    val deletedAt: LocalDateTime? = null

)


package com.miassolutions.milkledger.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.util.UUID

@Entity(
    tableName = "purchase_entry_table",
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
data class PurchaseEntryEntity(
    @PrimaryKey val purchaseId: String = UUID.randomUUID().toString(),
    val supplierId: String,
    val date: LocalDate = LocalDate.now(),
    val volume: Double,
    val fat: Double,
    val lr: Double,
    val ts: Double,
    val price: Double,
    val paid : Double,
    val balance : Double,
    val rateUsed: Double,
    val notes: String? = null
)


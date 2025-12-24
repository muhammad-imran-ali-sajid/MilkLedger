package com.miassolutions.milkledger.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "sales_table",
    foreignKeys = [
        ForeignKey(
            entity = CustomerEntity::class,
            parentColumns = ["customerId"],
            childColumns = ["customerId"],
            onDelete = ForeignKey.Companion.CASCADE
        )
    ],
    indices = [
        Index("customerId"),
        Index("dateMillis")
    ]
)
data class SalesEntity(
    @PrimaryKey
    val saleId: String = UUID.randomUUID().toString(),

    // Relations
    val customerId: String,

    // Ledger dates (Long-only)
    val dateMillis: Long,          // sale / ledger date
    val paidAtMillis: Long?,       // when payment actually received (nullable)

    // Business values (NO defaults)
    val volume: Double,
    val deduction: Double,
    val netMilk: Double,
    val price: Double,
    val paid: Double,
    val balance: Double,
    val rateUsed: Double,
    val notes: String?,

    // System
    val updatedAtMillis: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false,
    val deletedAtMillis: Long? = null
)
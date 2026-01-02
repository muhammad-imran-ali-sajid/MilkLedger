package com.miassolutions.milkledger.features.sale.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "sales_table",
    indices = [Index("customerId"), Index("dateMillis")]
)
data class SaleEntity(
    @PrimaryKey val saleId: String = UUID.randomUUID().toString(),
    val customerId: String,
    val dateMillis: Long,          // Ledger Date
    val paidAtMillis: Long?,

    // Milk Details
    val volume: Double,
    val deduction: Double,
    val netMilk: Double,

    // Price Details
    val rateUsed: Double,
    val totalAmount: Double,       // Total Bill
    val paid: Double,        // Sirf record ke liye (Ledger math Transaction se hoga)
    val balance: Double,

    val notes: String?,

    // Sync
    val createdAtMillis: Long = System.currentTimeMillis(),
    val updatedAtMillis: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false,
    val deletedAtMillis: Long? = null
)
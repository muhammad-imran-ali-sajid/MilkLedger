package com.miassolutions.milkledger.presentation.customer.sales.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.miassolutions.milkledger.data.local.entities.CustomerEntity
import java.time.LocalDate
import java.time.LocalDateTime
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
    indices = [Index("customerId"), Index("date")]
)
data class SalesEntity(
    @PrimaryKey val saleId: String = UUID.randomUUID().toString(),
    val customerId: String = "",
    val date: LocalDate,   // ledger date
    val volume: Double,
    val deduction: Double,
    val netMilk: Double,
    val price: Double,
    val paid: Double,
    val balance: Double,
    val rateUsed: Double,                    // snapshot of rate at entry time
    val notes: String? = null,

    // NEW FIELDS
    val paidDate: LocalDate? = null,

    val isSynced: Boolean = false,
    val updatedAt: String = LocalDateTime.now().toString(),
    val deletedAt: String? = null

)
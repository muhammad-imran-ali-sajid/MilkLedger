package com.miassolutions.milkledger.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.util.UUID

@Entity(
    tableName = "sales_entry_table",
    foreignKeys = [
        ForeignKey(
            entity = CustomerEntity::class,
            parentColumns = ["customerId"],
            childColumns = ["customerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("customerId"), Index("date")]
)
data class SalesEntryEntity(
    @PrimaryKey val saleId: String = UUID.randomUUID().toString(),
    val customerId: String,
    val date: LocalDate = LocalDate.now(),   // ledger date
    val volume: Double,
    val deduction: Double,
    val netMilk: Double,
    val price: Double,
    val paid : Double,
    val balance : Double,
    val rateUsed: Double,                    // snapshot of rate at entry time
    val notes: String? = null
)


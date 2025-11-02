package com.miassolutions.milkledger.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.util.UUID

@Entity(tableName = "customer_table")
data class CustomerEntity(
    @PrimaryKey val customerId: String = UUID.randomUUID().toString(),
    val customerName: String,
    val customerRate: Double = 0.0,
    val sortOrder: Int = 1,
    val advanceAmount: Double = 0.0,
    val createdAt: String = LocalDate.now().toString(),
    val isDefault: Boolean = false,

    var isSynced: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis(),
    val deletedAt: Long? = null
)
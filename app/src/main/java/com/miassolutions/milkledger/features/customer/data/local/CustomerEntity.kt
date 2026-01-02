package com.miassolutions.milkledger.features.customer.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "customer_table")
data class CustomerEntity(
    @PrimaryKey
    val customerId: String = UUID.randomUUID().toString(),
    val customerName: String,
    val customerRate: Double,
    val sortOrder: Int = 0,
    val advanceAmount: Double,
    val isDefault: Boolean = false,


    val createdAtMillis: Long = System.currentTimeMillis(),
    val updatedAtMillis: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false,
    val deletedAtMillis: Long? = null
)
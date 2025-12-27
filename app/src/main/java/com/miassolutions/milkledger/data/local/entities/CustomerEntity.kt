package com.miassolutions.milkledger.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "customer_table",
    indices = [Index(value = ["sortOrder"], unique = true)]
)
data class CustomerEntity(
    @PrimaryKey
    val customerId: String = UUID.randomUUID().toString(),

    // Business fields (NO defaults)
    val customerName: String,
    val customerRate: Double,
    val sortOrder: Int,
    val advanceAmount: Double,
    val isDefault: Boolean, // permanent true; temporary false

    // System fields
    val createdAtMillis: Long = System.currentTimeMillis(),
    val updatedAtMillis: Long = System.currentTimeMillis(),

    val isSynced: Boolean = false,
    val deletedAtMillis: Long? = null
)


package com.miassolutions.milkledger.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

@Entity(tableName = "customer_table")
data class CustomerEntity(
    @PrimaryKey
    val customerId: String = UUID.randomUUID().toString(),
    val customerName: String="",        // ✅ Default value added
    val customerRate: Double=0.0,
    val sortOrder: Int = 1,
    val advanceAmount: Double=0.0,
    val createdAt: String = LocalDateTime.now().toString(),
    val isDefault: Boolean = false,

    var isSynced: Boolean = false,
    val updatedAt: String = LocalDateTime.now().toString(),
    val deletedAt: Long? = null
)

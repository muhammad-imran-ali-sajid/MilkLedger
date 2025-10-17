package com.miassolutions.milkledger.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.util.UUID

@Entity(tableName = "customer_table")
data class CustomerEntity(
    @PrimaryKey val customerId: String = UUID.randomUUID().toString(),
    val customerName: String,
    val customerRate: Double,  // default rate, can be changed later
    val sortOrder: Int = 0,
    val createdAt: LocalDate = LocalDate.now(),
    val isDefault: Boolean = false
)
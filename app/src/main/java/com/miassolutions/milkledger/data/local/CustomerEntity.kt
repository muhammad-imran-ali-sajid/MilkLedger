package com.miassolutions.milkledger.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity("customer_table")
data class CustomerEntity(
    @PrimaryKey
    val customerId: String = UUID.randomUUID().toString(),
    val customerName: String,
    val customerRate: Double?,
)

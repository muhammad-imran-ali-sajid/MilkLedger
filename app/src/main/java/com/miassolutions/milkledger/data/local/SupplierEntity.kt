package com.miassolutions.milkledger.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity("supplier_table")
data class SupplierEntity(
    @PrimaryKey
    val supplierId: String = UUID.randomUUID().toString(),
    val supplierName: String,
    val supplierRate: Double,
)

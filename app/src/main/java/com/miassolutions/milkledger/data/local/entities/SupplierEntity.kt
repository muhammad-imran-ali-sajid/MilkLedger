package com.miassolutions.milkledger.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.util.UUID

@Entity(tableName = "supplier_table")
data class SupplierEntity(
    @PrimaryKey val supplierId: String = UUID.randomUUID().toString(),
    val supplierName: String,
    val supplierRate: Double,
    val createdAt: LocalDate = LocalDate.now()
)

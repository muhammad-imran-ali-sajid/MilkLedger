package com.miassolutions.milkledger.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

@Entity(tableName = "supplier_table")
data class SupplierEntity(
    @PrimaryKey val supplierId: String = UUID.randomUUID().toString(),
    val supplierName: String,
    val supplierRate: Double,
    val sortOrder: Int = 0,
    val advanceAmount: Double = 0.0,
    val createdAt: String = LocalDateTime.now().toString(),
    val isDefault: Boolean = false,

    val isSynced: Boolean = false,
    val updatedAt: String = LocalDateTime.now().toString(),
    val deletedAt: Long? = null

)

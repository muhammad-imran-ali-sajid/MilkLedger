package com.miassolutions.milkledger.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

//
//@Entity(tableName = "supplier_table")
//data class SupplierEntity(
//    @PrimaryKey
//    val supplierId: String = UUID.randomUUID().toString(),
//
//    val supplierName: String,
//    val supplierRate: Double,
//    val sortOrder: Int,
//    val advanceAmount: Double,
//    val isDefault: Boolean,
//
//    val createdAtMillis: Long = System.currentTimeMillis(),
//    val updatedAtMillis: Long = System.currentTimeMillis(),
//    val isSynced: Boolean = false,
//    val deletedAtMillis: Long? = null
//)

@Entity(tableName = "supplier_table")
data class SupplierEntity(
    @PrimaryKey val supplierId: String = UUID.randomUUID().toString(),
    val supplierName: String,
    val supplierRate: Double,
    val sortOrder: Int = 0,
    val advanceAmount: Double,
    val isDefault: Boolean = false,

    val createdAtMillis: Long = System.currentTimeMillis(),
    val updatedAtMillis: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false,
    val deletedAtMillis: Long? = null
)

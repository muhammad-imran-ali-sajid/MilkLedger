package com.miassolutions.milkledger.data.mapper

import com.miassolutions.milkledger.data.local.entities.SupplierEntity
import com.miassolutions.milkledger.domain.model.Supplier

fun SupplierEntity.toDomain(): Supplier =
    Supplier(
        id = supplierId,
        name = supplierName,
        rate = supplierRate,
        sortOrder = sortOrder,
        advanceAmount = advanceAmount,
        isDefault = isDefault
    )

fun Supplier.toEntity(): SupplierEntity =
    SupplierEntity(
        supplierId = id,
        supplierName = name,
        supplierRate = rate,
        sortOrder = sortOrder,
        advanceAmount = advanceAmount,
        isDefault = isDefault
    )

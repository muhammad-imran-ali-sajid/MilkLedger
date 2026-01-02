package com.miassolutions.milkledger.features.supplier

import com.miassolutions.milkledger.features.supplier.data.local.SupplierEntity
import com.miassolutions.milkledger.features.supplier.domain.Supplier


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

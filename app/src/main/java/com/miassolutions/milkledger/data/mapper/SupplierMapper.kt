package com.miassolutions.milkledger.data.mapper

import com.miassolutions.milkledger.data.local.entities.SupplierEntity
import com.miassolutions.milkledger.domain.model.Supplier
import java.util.UUID

fun Supplier.toEntity(): SupplierEntity = SupplierEntity(
    supplierId = this.id ?: UUID.randomUUID().toString(),
    supplierName = this.name,
    supplierRate = this.rate,
    sortOrder = this.sortOrder

)

fun SupplierEntity.toDomain(): Supplier = Supplier(
    id = this.supplierId,
    name = this.supplierName,
    rate = this.supplierRate,
    sortOrder = this.sortOrder
)


package com.miassolutions.milkledger.data.mapper

import com.miassolutions.milkledger.data.local.CustomerEntity
import com.miassolutions.milkledger.data.local.SupplierEntity
import com.miassolutions.milkledger.domain.model.Customer
import com.miassolutions.milkledger.domain.model.Supplier
import java.util.UUID

fun Supplier.toEntity(): SupplierEntity = SupplierEntity(
    supplierId = this.id ?: UUID.randomUUID().toString(),
    supplierName = this.name,
    supplierRate = this.rate,

    )

fun SupplierEntity.toDomain(): Supplier = Supplier(
    id = this.supplierId,
    name = this.supplierName,
    rate = this.supplierRate,
)


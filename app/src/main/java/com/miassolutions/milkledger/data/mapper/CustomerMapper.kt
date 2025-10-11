package com.miassolutions.milkledger.data.mapper

import com.miassolutions.milkledger.data.local.entities.CustomerEntity
import com.miassolutions.milkledger.domain.model.Customer
import java.util.UUID

fun Customer.toEntity(): CustomerEntity = CustomerEntity(
    customerId = this.id ?: UUID.randomUUID().toString(),
    customerName = this.name,
    customerRate = this.rate,

    )

fun CustomerEntity.toDomain(): Customer = Customer(
    id = this.customerId,
    name = this.customerName,
    rate = this.customerRate,
)


package com.miassolutions.milkledger.data.mapper

import com.miassolutions.milkledger.data.local.entities.CustomerEntity
import com.miassolutions.milkledger.domain.model.Customer

fun CustomerEntity.toDomain(): Customer =
    Customer(
        id = customerId,
        name = customerName,
        rate = customerRate,
        sortOrder = sortOrder,
        advanceAmount = advanceAmount,
        isDefault = isDefault
    )

fun Customer.toEntity(): CustomerEntity =
    CustomerEntity(
        customerId = id,
        customerName = name,
        customerRate = rate,
        sortOrder = sortOrder,
        advanceAmount = advanceAmount,
        isDefault = isDefault
    )

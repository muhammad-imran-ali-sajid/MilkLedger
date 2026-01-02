package com.miassolutions.milkledger.features.customer.data.mapper


import com.miassolutions.milkledger.features.customer.data.local.CustomerEntity
import com.miassolutions.milkledger.features.customer.domain.Customer

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

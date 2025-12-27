package com.miassolutions.milkledger.presentation.customer.mapper

import com.miassolutions.milkledger.domain.model.Customer
import com.miassolutions.milkledger.presentation.customer.model.CustomerUi

fun Customer.toUI() = CustomerUi(
    id = id,
    name = name,
    rate = rate,
    sortOrder = sortOrder,
    advanceAmount = advanceAmount,
    isDefault = isDefault,

)

fun CustomerUi.toDomain() = Customer(
    id = id,
    name = name,
    rate = rate,
    sortOrder = sortOrder,
    advanceAmount = advanceAmount,
    isDefault = isDefault
)
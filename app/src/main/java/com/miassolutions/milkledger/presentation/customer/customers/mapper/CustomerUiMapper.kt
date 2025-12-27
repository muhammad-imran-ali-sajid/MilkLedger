package com.miassolutions.milkledger.presentation.customer.customers.mapper

import com.miassolutions.milkledger.domain.model.Customer
import com.miassolutions.milkledger.presentation.customer.customers.model.CustomerUi

fun Customer.toUI() = CustomerUi(
    id = id,
    name = name,
    displayRate = "Rs. $rate",
    sortOrder = sortOrder,
    displayAdvanceAmount = "Rs. $advanceAmount",
    isDefault = isDefault,

)

fun CustomerUi.toDomain() = Customer(
    id = id,
    name = name,
    rate = displayRate.removePrefix(" %").toDoubleOrNull() ?: 0.0,
    sortOrder = displayRate.toIntOrNull()?: 0,
    advanceAmount = displayAdvanceAmount.removePrefix(" %").toDoubleOrNull() ?: 0.0,
    isDefault = isDefault
)
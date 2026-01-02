package com.miassolutions.milkledger.features.customer.ui.mapper

import com.miassolutions.milkledger.features.customer.domain.Customer
import com.miassolutions.milkledger.features.customer.ui.model.CustomerUi
import com.miassolutions.milkledger.features.customer.ui.model.DropDownCustomerListUi


fun Customer.toUI(): CustomerUi = CustomerUi(
    id = id,
    name = name,
    rate = rate,
    sortOrder = sortOrder,
    advanceAmount = advanceAmount,
    isDefault = isDefault,

    )

fun CustomerUi.toDomain(): Customer = Customer(
    id = id,
    name = name,
    rate = rate,
    sortOrder = sortOrder,
    advanceAmount = advanceAmount,
    isDefault = isDefault
)

fun List<Customer>.toUiList() : List<CustomerUi> = this.map { it.toUI() }

fun Customer.toDropDownUi() = DropDownCustomerListUi(
    id = id,
    name = name,
    rate = rate
)
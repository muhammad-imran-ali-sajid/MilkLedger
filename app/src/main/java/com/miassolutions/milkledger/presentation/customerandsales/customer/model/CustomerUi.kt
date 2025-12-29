package com.miassolutions.milkledger.presentation.customerandsales.customer.model


data class CustomerUi(
    val id: String,
    val name: String,
    val rate: Double,
    val sortOrder: Int = 0,
    val advanceAmount: Double = 0.0,
    val isDefault: Boolean = true,
    val isExpanded: Boolean = false
)

data class DropDownCustomerListUi(
    val id: String,
    val name: String,
    val rate: Double
)
package com.miassolutions.milkledger.features.customer.domain

data class Customer(
    val id: String,
    val name: String,
    val rate: Double,
    val sortOrder: Int,
    val advanceAmount: Double,
    val isDefault: Boolean
)
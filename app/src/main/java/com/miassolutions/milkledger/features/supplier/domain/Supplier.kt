package com.miassolutions.milkledger.features.supplier.domain

data class Supplier(
    val id: String,
    val name: String,
    val rate: Double,
    val sortOrder: Int,
    val advanceAmount: Double,
    val isDefault: Boolean
)
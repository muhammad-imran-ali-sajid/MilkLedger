package com.miassolutions.milkledger.presentation.supplier.model

data class SupplierUi(
    val id: String,
    val name: String,
    val rate: Double,
    val sortOrder: Int,
    val advanceAmount: Double,
    val isDefault: Boolean,
    val isExpanded: Boolean = false
)
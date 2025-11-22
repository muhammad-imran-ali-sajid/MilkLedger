package com.miassolutions.milkledger.presentation.stats


data class StateRecord(
    val name: String,
    val amount: Double,
    val category: Category
) {
    enum class Category {
        CUSTOMER, SUPPLIER, EXPENSE
    }
}


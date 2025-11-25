package com.miassolutions.milkledger.presentation.stats


data class StateRecord(
    val name: String,
    val amount: Double,
    val volume : Double?=null ,
    val category: Category
) {
    enum class Category {
        CUSTOMER, SUPPLIER, EXPENSE, OTHER, PROFIT
    }
}


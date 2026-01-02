package com.miassolutions.milkledger.features.cashflow


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


package com.miassolutions.milkledger.presentation.expenses

enum class ExpenseType(val label: String) {
    FUEL("Fuel"),
    VEHICLE("Vehicle"),
    REFRESHMENT("Refreshment"),
    PERSONAL("Personal");

    override fun toString(): String = label
}
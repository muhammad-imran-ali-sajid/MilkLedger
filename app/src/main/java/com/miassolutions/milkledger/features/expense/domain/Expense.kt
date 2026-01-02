package com.miassolutions.milkledger.features.expense.domain

import java.time.LocalDate

data class Expense(
    val id: String,
    val date: LocalDate,
    val title: String,
    val amount: Double,
    val note: String?,
    val isBusiness: Boolean
)
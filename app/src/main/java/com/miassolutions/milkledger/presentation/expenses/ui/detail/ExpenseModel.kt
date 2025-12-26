package com.miassolutions.milkledger.presentation.expenses.ui.detail

import com.miassolutions.milkledger.core.extensions.toPriceStr
import com.miassolutions.milkledger.presentation.expenses.data.ExpensesEntity

fun ExpensesEntity.toModel() : ExpenseModel{
    return ExpenseModel(
        title = expenseTitle,
        amount = expenseAmount.toPriceStr()
    )
}


data class ExpenseModel(
    val title : String,
    val amount : String
)
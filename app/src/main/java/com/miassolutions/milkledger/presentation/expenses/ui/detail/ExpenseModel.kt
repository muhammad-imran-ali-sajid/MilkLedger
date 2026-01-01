package com.miassolutions.milkledger.presentation.expenses.ui.detail

import com.miassolutions.milkledger.utils.extensions.toPriceStr
import com.miassolutions.milkledger.data.local.entities.ExpensesEntity

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
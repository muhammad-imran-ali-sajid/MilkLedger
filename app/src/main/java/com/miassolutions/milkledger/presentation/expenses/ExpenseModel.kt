package com.miassolutions.milkledger.presentation.expenses

import com.miassolutions.milkledger.core.util.toPriceStr
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
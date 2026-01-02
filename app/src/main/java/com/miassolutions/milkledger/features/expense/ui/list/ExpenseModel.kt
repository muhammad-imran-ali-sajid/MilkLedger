package com.miassolutions.milkledger.features.expense.ui.list

import com.miassolutions.milkledger.features.expense.data.local.ExpenseEntity
import com.miassolutions.milkledger.utils.extensions.toPriceStr

fun ExpenseEntity.toModel() : ExpenseModel{
    return ExpenseModel(
        title = expenseTitle,
        amount = expenseAmount.toPriceStr()
    )
}


data class ExpenseModel(
    val title : String,
    val amount : String
)
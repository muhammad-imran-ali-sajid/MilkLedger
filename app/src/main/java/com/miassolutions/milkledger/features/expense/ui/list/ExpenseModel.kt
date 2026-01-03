package com.miassolutions.milkledger.features.expense.ui.list

import com.miassolutions.milkledger.core.localdb.expense.ExpenseEntity
import com.miassolutions.milkledger.utils.extensions.toPriceStr

fun ExpenseEntity.toModel() : ExpenseModel{
    return ExpenseModel(
        title = title,
        amount = amount.toDouble().toPriceStr()
    )
}


data class ExpenseModel(
    val title : String,
    val amount : String
)
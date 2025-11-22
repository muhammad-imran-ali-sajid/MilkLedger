package com.miassolutions.milkledger.data.mapper

import com.miassolutions.milkledger.data.local.entities.ExpensesEntity
import com.miassolutions.milkledger.presentation.stats.ExpenseSummary

fun ExpensesEntity.toExpenseSummary() = with(this){
    ExpenseSummary(
        expenseTitle = expenseTitle,
        expenseAmount = expenseAmount,
    )
}
package com.miassolutions.milkledger.data.mapper

import com.miassolutions.milkledger.data.local.entities.ExpensesEntity
import com.miassolutions.milkledger.presentation.stats.BusinessExpenseSummary

fun ExpensesEntity.toExpenseSummary() = with(this){
    BusinessExpenseSummary(
        expenseTitle = expenseTitle,
        expenseAmount = expenseAmount,
    )
}
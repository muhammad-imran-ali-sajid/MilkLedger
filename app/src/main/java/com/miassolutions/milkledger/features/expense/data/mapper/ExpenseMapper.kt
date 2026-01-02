package com.miassolutions.milkledger.features.expense.data.mapper

import com.miassolutions.milkledger.features.expense.data.local.ExpenseEntity
import com.miassolutions.milkledger.features.expense.domain.Expense
import com.miassolutions.milkledger.utils.extensions.toLocalDate
import com.miassolutions.milkledger.utils.extensions.toMillis


fun ExpenseEntity.toDomain(): Expense =
    Expense(
        id = expenseId,
        date = dateMillis.toLocalDate(),
        title = expenseTitle,
        amount = expenseAmount,
        note = note,
        isBusiness = isBusiness
    )


fun Expense.toEntity(): ExpenseEntity =
    ExpenseEntity(
        expenseId = id,
        dateMillis = date.toMillis(),
        expenseTitle = title,
        expenseAmount = amount,
        note = note,
        isBusiness = isBusiness
    )


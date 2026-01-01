package com.miassolutions.milkledger.presentation.expenses.data

import com.miassolutions.milkledger.utils.extensions.toLocalDate
import com.miassolutions.milkledger.utils.extensions.toMillis
import com.miassolutions.milkledger.data.local.entities.ExpensesEntity
import com.miassolutions.milkledger.domain.model.Expense

fun ExpensesEntity.toDomain(): Expense =
    Expense(
        id = expenseId,
        date = dateMillis.toLocalDate(),
        title = expenseTitle,
        amount = expenseAmount,
        note = note,
        isBusiness = isBusiness
    )


fun Expense.toEntity(): ExpensesEntity =
    ExpensesEntity(
        expenseId = id,
        dateMillis = date.toMillis(),
        expenseTitle = title,
        expenseAmount = amount,
        note = note,
        isBusiness = isBusiness
    )


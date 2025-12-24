package com.miassolutions.milkledger.data.mapper

import com.miassolutions.milkledger.core.extensions.toLocalDate
import com.miassolutions.milkledger.core.extensions.toMillis
import com.miassolutions.milkledger.data.local.entities.ExpensesEntity
import com.miassolutions.milkledger.domain.model.Expense

fun ExpensesEntity.toDomain(): Expense =
    Expense(
        id = expenseId,
        date = dateMillis.toLocalDate(),
        title = expenseTitle,
        amount = expenseAmount,
        note = expenseNote,
        isDefault = isDefault
    )


fun Expense.toEntity(): ExpensesEntity =
    ExpensesEntity(
        expenseId = id,
        dateMillis = date.toMillis(),
        expenseTitle = title,
        expenseAmount = amount,
        expenseNote = note,
        isDefault = isDefault
    )


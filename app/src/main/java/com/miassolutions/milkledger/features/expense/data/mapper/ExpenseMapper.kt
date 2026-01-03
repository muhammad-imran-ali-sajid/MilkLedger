package com.miassolutions.milkledger.features.expense.data.mapper

import com.miassolutions.milkledger.core.localdb.expense.ExpenseEntity
import com.miassolutions.milkledger.features.expense.domain.Expense
import com.miassolutions.milkledger.utils.extensions.toLocalDate
import com.miassolutions.milkledger.utils.extensions.toMillis


fun ExpenseEntity.toDomain(): Expense =
    Expense(
        id = expenseId,
        date = dateMillis.toLocalDate(),
        title = title,
        amount = amount,
        note = note,
        isPersonal = isPersonal
    )


fun Expense.toEntity(): ExpenseEntity =
    ExpenseEntity(
        expenseId = id,
        dateMillis = date.toMillis(),
        title = title,
        amount = amount,
        note = note,
        isPersonal = isPersonal
    )


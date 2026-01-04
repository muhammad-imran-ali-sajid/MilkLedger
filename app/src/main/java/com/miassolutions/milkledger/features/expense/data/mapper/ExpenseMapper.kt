package com.miassolutions.milkledger.features.expense.data.mapper

import com.miassolutions.milkledger.core.localdb.expense.ExpenseEntity
import com.miassolutions.milkledger.features.expense.domain.Expense
import com.miassolutions.milkledger.utils.extensions.toLocalDate
import com.miassolutions.milkledger.utils.extensions.toMillis
import java.util.UUID


fun ExpenseEntity.toDomain(): Expense {
    return Expense(
        expenseId = this.expenseId,
        date = this.dateMillis.toLocalDate(),
        title = this.title,
        amount = this.amount,
        category = this.category,
        isPersonal = this.isPersonal,
        note = this.note,
    )
}

fun Expense.toEntity(): ExpenseEntity {
    return ExpenseEntity(
        expenseId = this.expenseId.ifBlank { UUID.randomUUID().toString() },
        dateMillis = this.date.toMillis(),
        title = this.title,
        amount = this.amount,
        category = this.category,
        isPersonal = this.isPersonal,
        note = this.note,
    )
}


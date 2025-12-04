package com.miassolutions.milkledger.data.mapper

import com.miassolutions.milkledger.data.local.entities.ExpensesEntity
import com.miassolutions.milkledger.data.remote.model.FirestoreExpense
import java.time.LocalDate
import java.time.LocalDateTime

fun ExpensesEntity.toFirestoreModel(): FirestoreExpense {
    return FirestoreExpense(
        id = expenseId,
        date = date.toString(),
        title = expenseTitle,
        amount = expenseAmount,
        expenseNote = expenseNote ?: "",
        default = isDefault,
        isSynced = isSynced,
        createdAt = createdAt,
        updatedAt = updatedAt,
        deletedAt = deletedAt?.toString()
    )
}

fun List<ExpensesEntity>.toFirestoreModelList(): List<FirestoreExpense> {
    return this.map { it.toFirestoreModel() }
}

fun FirestoreExpense.toEntityModel(): ExpensesEntity {
    return ExpensesEntity(
        expenseId = id,
        date = LocalDate.parse(date),
        expenseTitle = title,
        expenseAmount = amount,
        expenseNote = expenseNote,
        isDefault = default ?: false,
        createdAt = createdAt,
        isSynced = isSynced,
        updatedAt = updatedAt,
        deletedAt = deletedAt.takeIf { it.isNullOrBlank() }?.let { LocalDateTime.parse(it) }
    )
}
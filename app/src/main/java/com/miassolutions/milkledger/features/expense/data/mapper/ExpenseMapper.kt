package com.miassolutions.milkledger.features.expense.data.mapper

import com.miassolutions.milkledger.core.contstants.Constants
import com.miassolutions.milkledger.core.contstants.Constants.PREFIX_EXPENSE
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

    // 1️⃣ ID LOGIC: Ensure Prefix exists
    // Agar Constants import nahi ho rahay to direct "exp_" likh den
    val prefix = Constants.PREFIX_EXPENSE // ya Constants.PREFIX_EXPENSE

    val finalId = when {
        // Case A: New Entry (ID Blank hai)
        this.expenseId.isBlank() -> prefix + UUID.randomUUID().toString()

        // Case B: Old Entry without prefix (Safety Check)
        !this.expenseId.startsWith(prefix) -> prefix + this.expenseId

        // Case C: Already has prefix (Update case)
        else -> this.expenseId
    }

    return ExpenseEntity(
        expenseId = finalId, // ✅ Updated ID
        dateMillis = this.date.toMillis(),
        title = this.title,
        amount = this.amount,
        category = this.category,
        isPersonal = this.isPersonal,
        note = this.note,
    )
}
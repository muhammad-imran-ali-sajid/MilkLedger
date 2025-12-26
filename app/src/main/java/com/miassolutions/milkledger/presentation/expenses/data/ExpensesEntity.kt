package com.miassolutions.milkledger.presentation.expenses.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "expense_table")
data class ExpensesEntity(
    @PrimaryKey
    val expenseId: String = UUID.randomUUID().toString(),

    val dateMillis: Long,
    val expenseTitle: String,
    val expenseAmount: Double,
    val expenseNote: String?,
    val isBusiness: Boolean,

    val createdAtMillis: Long = System.currentTimeMillis(),
    val updatedAtMillis: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false,
    val deletedAtMillis: Long? = null
)
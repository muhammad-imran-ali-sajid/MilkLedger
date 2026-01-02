package com.miassolutions.milkledger.features.expense.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "expense_table", indices = [Index("dateMillis")])
data class ExpenseEntity(
    @PrimaryKey
    val expenseId: String = UUID.randomUUID().toString(),
    val dateMillis: Long,
    val expenseTitle: String,
    val expenseAmount: Double,
    val note: String?,

    val isBusiness: Boolean,

    val createdAtMillis: Long = System.currentTimeMillis(),
    val updatedAtMillis: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false,
    val deletedAtMillis: Long? = null
)
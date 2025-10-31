package com.miassolutions.milkledger.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.util.UUID

@Entity (tableName = "expense_table")
data class ExpensesEntity(
    @PrimaryKey
    val expenseId: String = UUID.randomUUID().toString(),
    val date: LocalDate = LocalDate.now(),
    val expenseTitle: String,
    val expenseAmount: Double = 0.0,
    val expenseNote: String? = null,
    val isDefault: Boolean = false,

    val isSynced: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis(),
    val deletedAt: Long? = null

)

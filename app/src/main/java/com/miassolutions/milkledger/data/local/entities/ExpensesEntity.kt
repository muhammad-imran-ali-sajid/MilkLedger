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
    val expenseAmount: Double,
    val expenseNote: String? = null
)

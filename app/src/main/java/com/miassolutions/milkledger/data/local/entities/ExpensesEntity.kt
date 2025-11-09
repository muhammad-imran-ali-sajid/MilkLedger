package com.miassolutions.milkledger.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

@Entity(tableName = "expense_table")
data class ExpensesEntity(
    @PrimaryKey
    val expenseId: String = UUID.randomUUID().toString(),
    val date: LocalDate = LocalDate.now(),
    val expenseTitle: String = "",
    val expenseAmount: Double = 0.0,
    val expenseNote: String? = null,
    val isDefault: Boolean = false,

    val createdAt: String,
    val isSynced: Boolean = false,
    val updatedAt: String = LocalDateTime.now().toString(),
    val deletedAt: LocalDateTime? = null

)

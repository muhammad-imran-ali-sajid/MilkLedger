package com.miassolutions.milkledger.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.util.UUID

@Entity
data class ExpensesEntity(
    @PrimaryKey
    val expenseId: String = UUID.randomUUID().toString(),
    val date: LocalDate = LocalDate.now(),
    val expenseName: String,
    val expense: Double,
    val expenseNote: String
)

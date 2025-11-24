package com.miassolutions.milkledger.data.local.entities

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID
@Parcelize
@Entity(tableName = "expense_table")
data class ExpensesEntity(
    @PrimaryKey
    val expenseId: String = UUID.randomUUID().toString(),
    val date: LocalDate,
    val expenseTitle: String = "",
    val expenseAmount: Double = 0.0,
    val expenseNote: String? = null,
    val isDefault: Boolean = true,
    val createdAt: String = LocalDateTime.now().toString(),
    val isSynced: Boolean = false,
    val updatedAt: String = LocalDateTime.now().toString(),
    val deletedAt: LocalDateTime? = null
) : Parcelable


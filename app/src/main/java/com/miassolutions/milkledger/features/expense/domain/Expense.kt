package com.miassolutions.milkledger.features.expense.domain

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.LocalDate

@Parcelize
data class Expense(
    val expenseId: String = "",
    val date: LocalDate,
    val title: String,
    val amount: Long,          // Paisa
    val category: String?,
    val isPersonal: Boolean,
    val note: String? = null,
) : Parcelable
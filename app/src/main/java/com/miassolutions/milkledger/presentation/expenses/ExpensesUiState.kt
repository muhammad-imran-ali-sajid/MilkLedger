package com.miassolutions.milkledger.presentation.expenses

import com.miassolutions.milkledger.data.local.entities.ExpensesEntity
import java.time.LocalDate



data class ExpensesUiState(
    val currentDate: LocalDate = LocalDate.now(),

    // Fixed (default) expenses
    val fixedExpenses: List<ExpensesEntity> = emptyList(),
    val fixedTotal: Double = 0.0,

    // Variable (user-added) expenses
    val variableExpenses: List<ExpensesEntity> = emptyList(),
    val variableTotal: Double = 0.0,

    val isLoading: Boolean = false,
    val error: String? = null
)



sealed interface ExpensesUiEvent {
    data class SelectDate(val date: LocalDate) : ExpensesUiEvent
}

package com.miassolutions.milkledger.presentation.expenses

import com.miassolutions.milkledger.data.local.entities.ExpensesEntity
import java.time.LocalDate



data class ExpensesUiState(
    val expensesList: List<ExpensesEntity> = emptyList(),
    // currentDate is the source of truth for the displayed date
    val currentDate: LocalDate = LocalDate.now(),
    val todayTotalExpenses: Double = 0.0,
    val todayAvgExpenses: Double = 0.0,
    val isLoading: Boolean = false,
    val error: String? = null
)


sealed interface ExpensesUiEvent {
    data class SelectDate(val date: LocalDate) : ExpensesUiEvent
}

package com.miassolutions.milkledger.presentation.expenses

import com.miassolutions.milkledger.data.local.entities.ExpensesEntity
import java.time.LocalDate

data class ExpensesUiState(
    val currentDate: LocalDate = LocalDate.now(),
    val expensesList: List<ExpensesEntity> = emptyList(),
    val todayTotalExpenses: Double = 0.0,
    val todayAvgExpenses: Double = 0.0
)

sealed class ExpensesUiEvent {

    data class OnExpenseSelected(val expenseId: String) : ExpensesUiEvent()
    data object NextDate : ExpensesUiEvent()
    data object PrevDate : ExpensesUiEvent()
}
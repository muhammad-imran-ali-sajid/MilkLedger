package com.miassolutions.milkledger.presentation.expenses.ui.detail

import com.miassolutions.milkledger.presentation.expenses.data.ExpensesEntity
import java.time.LocalDate



data class ExpensesUiState(
    val currentDate: LocalDate = LocalDate.now(),

    val expensesList: List<ExpensesEntity> = emptyList(),
    val filteredList: List<ExpensesEntity> = emptyList(),

    val businessTotalExpenses: Double = 0.0,
    val personalTotalExpenses: Double = 0.0,
    val periodLabel : String = "",
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)





sealed interface ExpensesUiEvent {
    data class SelectDate(val date: LocalDate) : ExpensesUiEvent
}

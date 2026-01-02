package com.miassolutions.milkledger.features.expense.ui.list

import com.miassolutions.milkledger.features.expense.data.local.ExpenseEntity
import java.time.LocalDate


data class ExpensesUiState(
    val currentDate: LocalDate = LocalDate.now(),

    val expensesList: List<ExpenseEntity> = emptyList(),
    val filteredList: List<ExpenseEntity> = emptyList(),

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

package com.miassolutions.milkledger.features.expense.ui.list

import java.time.LocalDate


import com.miassolutions.milkledger.features.expense.domain.Expense

data class ExpenseListUiState(
    val isLoading: Boolean = false,
    val date: LocalDate = LocalDate.now(),
    val expenses: List<Expense> = emptyList(),

    // Stats
    val totalExpense: Long = 0, // Paisa
    val avgExpense: Double = 0.0,

    val error: String? = null
) {
    val displayTotal
        get() = totalExpense

    val displayAvgExpense
        get() = avgExpense
}

sealed interface ExpenseListUiEvent {
    data object OnNextDate : ExpenseListUiEvent
    data object OnPrevDate : ExpenseListUiEvent
    data object OnDateClick : ExpenseListUiEvent // Date Picker k liye
    data class OnDateSelected(val date: LocalDate) : ExpenseListUiEvent

    data object OnAddExpenseClicked : ExpenseListUiEvent
    data class OnExpenseClicked(val expense: Expense) : ExpenseListUiEvent
}

sealed interface ExpenseListUiEffect {
    data class NavigateToAddExpense(val dateMillis: Long) : ExpenseListUiEffect

    data class NavigateToEditExpense(val expense: Expense) : ExpenseListUiEffect
    data class ShowSnackbar(val message: String) : ExpenseListUiEffect
}

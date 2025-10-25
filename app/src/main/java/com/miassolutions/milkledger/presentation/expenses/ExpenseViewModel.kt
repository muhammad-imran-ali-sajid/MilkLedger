package com.miassolutions.milkledger.presentation.expenses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miassolutions.milkledger.data.local.entities.ExpensesEntity
import com.miassolutions.milkledger.data.repository.ExpensesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class ExpenseViewModel @Inject constructor(
    private val repository: ExpensesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExpensesUiState())
    val uiState: StateFlow<ExpensesUiState> = _uiState.asStateFlow()

    init {
        // Start collecting expenses for the initial date (which should be today)
        collectExpenses(_uiState.value.currentDate)
    }

    /**
     * Ensures static expense entries exist for the given date.
     * @param date The date to check and insert static expenses for.
     */
    private suspend fun ensureStaticExpensesForDate(date: LocalDate) {
        STATIC_TITLES.forEach { title -> // Now using the constant from companion object
            val exists = repository.expenseExistsForTitleAndDate(title, date)
            if (!exists) {
                repository.insertExpense(
                    ExpensesEntity(
                        expenseTitle = title,
                        expenseAmount = 0.0,
                        date = date,
                        // Other properties (like id or note) will use defaults
                    )
                )
            }
        }
    }

    /**
     * Main function to start collecting expenses for a specific date.
     * This ensures static items exist and starts the flow collection from the repository.
     *
     * @param date The date to load expenses for.
     */
    private fun collectExpenses(date: LocalDate) {
        viewModelScope.launch {
            // 1. Ensure static entries exist before loading
            ensureStaticExpensesForDate(date)

            // 2. Start collecting the flow of expenses for the new date
            repository.getAllExpensesForDate(date).collect { expenses ->
                val total = expenses.sumOf { it.expenseAmount }
                val avg = if (expenses.isNotEmpty()) total / expenses.size else 0.0

                _uiState.update {
                    it.copy(
                        expensesList = expenses,
                        todayTotalExpenses = total,
                        todayAvgExpenses = avg
                    )
                }
            }
        }
    }


    fun onEvent(event: ExpensesUiEvent) {
        when (event) {

            is ExpensesUiEvent.SelectDate -> {
                // Update the state and trigger data collection for the newly selected date
                _uiState.update { it.copy(currentDate = event.date) }
                collectExpenses(event.date)
            }
        }

    }

    fun insertExpense(expense: ExpensesEntity) = viewModelScope.launch {
        repository.insertExpense(expense)
    }

    fun updateExpense(expense: ExpensesEntity) = viewModelScope.launch {
        repository.updateExpense(expense)
    }

    fun deleteExpense(expense: ExpensesEntity) = viewModelScope.launch {
        repository.deleteExpense(expense)
    }

    // FIX: Moved staticTitles to a companion object to ensure initialization safety
    companion object {
        private val STATIC_TITLES = listOf("Fuel", "Wages")
    }
}

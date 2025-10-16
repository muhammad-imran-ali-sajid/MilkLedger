package com.miassolutions.milkledger.presentation.expenses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miassolutions.milkledger.data.local.entities.ExpensesEntity
import com.miassolutions.milkledger.data.repository.ExpensesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
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
        // Load initial data for today's date
        loadExpensesForDate(_uiState.value.currentDate)
    }

    fun onEvent(event: ExpensesUiEvent) {
        when (event) {
            is ExpensesUiEvent.OnExpenseSelected -> {
                // handle navigation or editing logic in fragment
            }

            ExpensesUiEvent.NextDate -> {
                val nextDate = _uiState.value.currentDate.plusDays(1)
                _uiState.update { it.copy(currentDate = nextDate) }
                loadExpensesForDate(nextDate)
            }

            ExpensesUiEvent.PrevDate -> {
                val prevDate = _uiState.value.currentDate.minusDays(1)
                _uiState.update { it.copy(currentDate = prevDate) }
                loadExpensesForDate(prevDate)
            }
        }
    }

    private fun loadExpensesForDate(date: LocalDate) {
        viewModelScope.launch {
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

    fun insertExpense(expense: ExpensesEntity) = viewModelScope.launch {
        repository.insertExpense(expense)
    }

    fun updateExpense(expense: ExpensesEntity) = viewModelScope.launch {
        repository.updateExpense(expense)
    }

    fun deleteExpense(expense: ExpensesEntity) = viewModelScope.launch {
        repository.deleteExpense(expense)
    }
}

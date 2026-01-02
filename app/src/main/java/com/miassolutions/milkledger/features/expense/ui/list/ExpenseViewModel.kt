package com.miassolutions.milkledger.features.expense.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miassolutions.milkledger.features.expense.data.repository.ExpenseRepository
import com.miassolutions.milkledger.features.expense.domain.Expense

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class ExpenseViewModel @Inject constructor(
    private val repository: ExpenseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExpensesUiState())
    val uiState = _uiState.asStateFlow()

    init {
        observeDate(LocalDate.now())
    }

    fun onDateSelected(date: LocalDate) {
        if (date == _uiState.value.currentDate) return
        observeDate(date)
    }

    fun goToNextDate() =
        onDateSelected(_uiState.value.currentDate.plusDays(1))

    fun goToPreviousDate() =
        onDateSelected(_uiState.value.currentDate.minusDays(1))

    fun deleteExpense(expenseId: String) = viewModelScope.launch {
        repository.deleteExpense(expenseId)
    }

    fun updateExpense(expense: Expense) = viewModelScope.launch {
        repository.updateExpense(expense)
    }

    private fun observeDate(date: LocalDate) {

    }
}

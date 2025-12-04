package com.miassolutions.milkledger.presentation.expenses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miassolutions.milkledger.core.util.formatPeriodLabel
import com.miassolutions.milkledger.data.local.entities.ExpensesEntity
import com.miassolutions.milkledger.data.repository.ExpensesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class ExpenseViewModel @Inject constructor(
    private val repository: ExpensesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExpensesUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadData(LocalDate.now())
    }

    // --------------------------------------------------------------------
    // PUBLIC API
    // --------------------------------------------------------------------

    fun onDateSelected(date: LocalDate) {
        if (date != _uiState.value.currentDate) {
            _uiState.update { it.copy(currentDate = date) }
            loadData(date)
        }
    }

    fun goToNextDate() {
        onDateSelected(_uiState.value.currentDate.plusDays(1))
    }

    fun goToPreviousDate() {
        onDateSelected(_uiState.value.currentDate.minusDays(1))
    }

    fun saveExpenses(list: List<ExpensesEntity>) = viewModelScope.launch {
        repository.upsertAllExpenses(list)
    }

    fun updateExpense(entry: ExpensesEntity) = viewModelScope.launch {
        repository.upsertExpense(entry)
    }

    fun deleteExpense(expense: ExpensesEntity) = viewModelScope.launch {
        repository.deleteExpense(expense)
    }

    // --------------------------------------------------------------------
    // CORE LOGIC — LOAD EXPENSES FOR SELECTED DATE
    // --------------------------------------------------------------------
    private fun loadData(date: LocalDate) {
        viewModelScope.launch {

            _uiState.update { it.copy(isLoading = true) }

            repository.getDailyExpenses(date).collectLatest { list ->

                val totalExpenses = list.sumOf { it.expenseAmount }
                val businessExpenses = list.filter { !it.isDefault }.sumOf { it.expenseAmount }
                val personalExpenses = totalExpenses - businessExpenses

                _uiState.update {
                    it.copy(
                        filteredList = list,
                        businessTotalExpenses = businessExpenses,
                        personalTotalExpenses = personalExpenses,
                        periodLabel = formatPeriodLabel(date, date),
                        startDate = date,
                        endDate = date,
                        isLoading = false
                    )
                }
            }
        }
    }
}

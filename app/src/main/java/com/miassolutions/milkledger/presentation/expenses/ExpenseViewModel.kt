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
import java.time.LocalDateTime
import java.util.UUID // <-- Added UUID import
import javax.inject.Inject

@HiltViewModel
class ExpenseViewModel @Inject constructor(
    private val repository: ExpensesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExpensesUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadForDate(LocalDate.now())
    }

    fun onEvent(event: ExpensesUiEvent) {
        when (event) {
            is ExpensesUiEvent.SelectDate -> loadForDate(event.date)
        }
    }

    private fun loadForDate(date: LocalDate) {

        // Update date in UiState
        _uiState.update { it.copy(currentDate = date, isLoading = true) }

        viewModelScope.launch {
            ensureDefaultExpenses(date)
        }

        collectFixedExpenses(date)
        collectVariableExpenses(date)
    }

    // --------------------------------------------------
    // ✔ Improved Default Expenses Logic (Fast + Clean)
    // --------------------------------------------------
    private suspend fun ensureDefaultExpenses(date: LocalDate) {
        val existingTitles = repository.getTitlesForDate(date).toSet()

        val missingDefaults = DEFAULT_TITLES.filter { it !in existingTitles }

        if (missingDefaults.isEmpty()) return

        val toInsert = missingDefaults.map { title ->
            ExpensesEntity(
                expenseId = UUID.randomUUID().toString(),
                createdAt = LocalDateTime.now().toString(),
                expenseTitle = title,
                expenseAmount = 0.0,
                isDefault = true,
                date = date
            )
        }

        repository.insertAll(toInsert)
    }

    // --------------------------------------------------
    // Separate collectors make UI simpler
    // --------------------------------------------------
    private fun collectFixedExpenses(date: LocalDate) {
        viewModelScope.launch {
            repository.getFixedExpenses(date).collect { list ->
                val total = list.sumOf { it.expenseAmount }

                _uiState.update {
                    it.copy(
                        fixedExpenses = list,
                        fixedTotal = total,
                        isLoading = false

                    )
                }
            }
        }
    }

    private fun collectVariableExpenses(date: LocalDate) {
        viewModelScope.launch {
            repository.getVariableExpenses(date).collect { list ->
                val total = list.sumOf { it.expenseAmount }

                _uiState.update {
                    it.copy(
                        variableExpenses = list,
                        variableTotal = total,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun insertExpense(expense: ExpensesEntity) = viewModelScope.launch {
        val final = if (expense.expenseId.isBlank()) {
            expense.copy(expenseId = UUID.randomUUID().toString())
        } else expense

        repository.insertExpense(final)
    }

    fun deleteExpense(expense: ExpensesEntity) = viewModelScope.launch {
        repository.deleteExpense(expense)
    }

    fun updateExpense(expense: ExpensesEntity) =
        viewModelScope.launch { repository.updateExpense(expense) }

    companion object {
        private val DEFAULT_TITLES = listOf("Fuel", "Meal", "Vehicle")
    }
}

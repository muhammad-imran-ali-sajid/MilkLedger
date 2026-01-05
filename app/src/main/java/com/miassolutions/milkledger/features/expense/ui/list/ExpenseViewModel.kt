package com.miassolutions.milkledger.features.expense.ui.list

import androidx.lifecycle.viewModelScope
import com.miassolutions.milkledger.core.ui.BaseViewModel
import com.miassolutions.milkledger.features.expense.data.repository.ExpenseRepository
import com.miassolutions.milkledger.features.expense.ui.list.ExpenseListUiEffect.*
import com.miassolutions.milkledger.utils.extensions.toMillis
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class ExpenseListViewModel @Inject constructor(
    private val repository: ExpenseRepository
) : BaseViewModel<ExpenseListUiState, ExpenseListUiEvent, ExpenseListUiEffect>(ExpenseListUiState()) {

    private var expenseJob: Job? = null

    init {
        loadExpenses(LocalDate.now())
    }

    override fun onEvent(event: ExpenseListUiEvent) {
        when (event) {
            ExpenseListUiEvent.OnNextDate -> {
                val newDate = currentState.date.plusDays(1)
                updateState { it.copy(date = newDate) }
                loadExpenses(newDate)
            }

            ExpenseListUiEvent.OnPrevDate -> {
                val newDate = currentState.date.minusDays(1)
                updateState { it.copy(date = newDate) }
                loadExpenses(newDate)
            }

            ExpenseListUiEvent.OnDateClick -> {
                // Fragment will handle Date Picker dialog
            }

            is ExpenseListUiEvent.OnDateSelected -> {
                updateState { it.copy(date = event.date) }
                loadExpenses(event.date)
            }

            ExpenseListUiEvent.OnAddExpenseClicked -> {
                val dateMillis = currentState.date.toMillis()
                emitEffect(NavigateToAddExpense(dateMillis))
            }

            is ExpenseListUiEvent.OnExpenseClicked -> {
                // Future: Open Edit Screen
            }

            is ExpenseListUiEvent.OnDeleteClicked -> {
                deleteExpense(event.id)
            }
        }
    }

    private fun deleteExpense(id: String) {
        viewModelScope.launch {
            repository.deleteExpense(id)
            emitEffect(ExpenseListUiEffect.ShowSnackbar("Expense deleted"))
        }
    }

    private fun loadExpenses(date: LocalDate) {
        // Purana flow cancel karein taake overlapping na ho
        expenseJob?.cancel()

        // Start aur End of Day calculate karein (Millis mein)
        val startOfDay = date.atStartOfDay().toMillis() // Aapka extension function
        val endOfDay = date.plusDays(1).atStartOfDay().toMillis() - 1

        updateState { it.copy(isLoading = true) }

        expenseJob = repository.getExpensesByDateRange(startOfDay, endOfDay)
            .onEach { list ->
                // Calculations
                val total = list.sumOf { it.amount }
                val avg = if (list.isNotEmpty()) total.toDouble() / list.size else 0.0

                updateState {
                    it.copy(
                        isLoading = false,
                        expenses = list,
                        totalExpense = total,
                        avgExpense = avg
                    )
                }
            }
            .launchIn(viewModelScope)
    }
}



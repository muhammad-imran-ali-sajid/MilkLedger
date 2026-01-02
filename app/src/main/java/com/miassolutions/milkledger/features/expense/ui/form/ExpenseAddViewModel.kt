package com.miassolutions.milkledger.features.expense.ui.form

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miassolutions.milkledger.features.expense.data.repository.ExpenseRepository
import com.miassolutions.milkledger.features.expense.ui.list.ExpenseAddUiState
import com.miassolutions.milkledger.features.expense.ui.list.ExpenseInput
import com.miassolutions.milkledger.features.expense.ui.list.ExpenseUiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class ExpenseAddViewModel @Inject constructor(
    private val repository: ExpenseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExpenseAddUiState())
    val uiState = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<ExpenseUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    fun onDateChanged(date: LocalDate) {
        _uiState.update { it.copy(date = date) }
    }

    fun addPersonalRow() {
        _uiState.update {
            it.copy(personalItems = it.personalItems + ExpenseInput())
        }
    }

    fun removePersonalRow(index: Int) {
        _uiState.update {
            it.copy(personalItems = it.personalItems.toMutableList().apply { removeAt(index) })
        }
    }

    fun updateBusinessAmount(title: String, value: String) {
        val amount = value.toDoubleOrNull() ?: 0.0
        _uiState.update {
            it.copy(
                businessInputs = it.businessInputs.map { input ->
                    if (input.title == title) input.copy(amount = amount) else input
                }
            )
        }
    }

    fun updatePersonalTitle(index: Int, value: String) {
        _uiState.update {
            it.copy(
                personalItems = it.personalItems.toMutableList().apply {
                    this[index] = this[index].copy(title = value)
                }
            )
        }
    }

    fun updatePersonalAmount(index: Int, value: String) {
        val amount = value.toDoubleOrNull() ?: 0.0
        _uiState.update {
            it.copy(
                personalItems = it.personalItems.toMutableList().apply {
                    this[index] = this[index].copy(amount = amount)
                }
            )
        }
    }


    fun save() = viewModelScope.launch {
//        val state = _uiState.value
//        val expenses = mutableListOf<ExpensesEntity>()
//
//        // BUSINESS (fixed)
//        state.businessInputs.forEach { input ->
//            if (input.amount > 0) {
//                expenses.add(input.toEntity(state.date, true))
//            }
//        }
//
//        // PERSONAL (variable)
//        state.personalItems.forEach { input ->
//            if (input.isValid()) {
//                expenses.add(input.toEntity(state.date, false))
//            }
//        }
//
//        if (expenses.isEmpty()) {
//            _uiEvent.emit(ExpenseUiEvent.ShowMessage("Enter at least one expense"))
//            return@launch
//        }
//
//        repository.upsertAllExpenses(expenses)
//        _uiEvent.emit(ExpenseUiEvent.Dismiss)
    }
}

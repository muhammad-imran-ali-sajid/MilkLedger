package com.miassolutions.milkledger.features.expense.ui.form

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miassolutions.milkledger.features.expense.data.repository.ExpenseRepository
import com.miassolutions.milkledger.features.expense.domain.Expense
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditExpenseViewModel @Inject constructor(
    private val repository: ExpenseRepository
) : ViewModel() {

    private val _updateStatus = MutableSharedFlow<String>() // Success/Error message
    val updateStatus = _updateStatus.asSharedFlow()

    fun updateExpense(expense: Expense) {
        viewModelScope.launch {
            try {
                repository.updateExpense(expense)
                _updateStatus.emit("Success")
            } catch (e: Exception) {
                _updateStatus.emit("Error: ${e.message}")
            }
        }
    }


    fun deleteExpense(id: String) {
        viewModelScope.launch {

            try {
                repository.deleteExpense(id)
                _updateStatus.emit("Success")
            } catch (e: Exception) {
                _updateStatus.emit("Error: ${e.message}")
            }
        }
    }
}
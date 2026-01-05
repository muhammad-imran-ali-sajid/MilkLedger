package com.miassolutions.milkledger.features.expense.ui.form

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miassolutions.milkledger.features.expense.data.repository.ExpenseRepository
import com.miassolutions.milkledger.features.expense.domain.Expense
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditExpenseViewModel @Inject constructor(
    private val repository: ExpenseRepository
) : ViewModel() {

    private val _updateStatus = Channel<String>() // Success/Error message
    val updateStatus = _updateStatus.receiveAsFlow()

    fun updateExpense(expense: Expense) {
        viewModelScope.launch {
            try {
                repository.updateExpense(expense)
                _updateStatus.send("Success")
            } catch (e: Exception) {
                _updateStatus.send("Error: ${e.message}")
            }
        }
    }
}
package com.miassolutions.milkledger.presentation.expenses

import androidx.lifecycle.ViewModel
import com.miassolutions.milkledger.data.repository.ExpensesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ExpenseViewModel @Inject constructor(private val repository: ExpensesRepository) :
    ViewModel() {
}
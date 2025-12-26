package com.miassolutions.milkledger.presentation.settings

import androidx.lifecycle.ViewModel
import com.miassolutions.milkledger.data.repository.CustomerRepository
import com.miassolutions.milkledger.data.repository.NoteRepository
import com.miassolutions.milkledger.data.repository.SupplierRepository
import com.miassolutions.milkledger.presentation.expenses.data.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject


@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val customerRepo: CustomerRepository,
    private val supplierRepo: SupplierRepository,
    private val expensesRepo: ExpenseRepository,
    private val notesRepo: NoteRepository

) : ViewModel() {


}

package com.miassolutions.milkledger.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miassolutions.milkledger.data.repository.CustomerRepository
import com.miassolutions.milkledger.data.repository.ExpensesRepository
import com.miassolutions.milkledger.data.repository.NoteRepository
import com.miassolutions.milkledger.data.repository.SupplierRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.launch


@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val customerRepo: CustomerRepository,
    private val supplierRepo: SupplierRepository,
    private val expensesRepo: ExpensesRepository,
    private val notesRepo: NoteRepository

) : ViewModel() {

    fun manualSync() {
        viewModelScope.launch {
            customerRepo.synchronizeCustomers()
            supplierRepo.synchronizeSuppliers()
            expensesRepo.synchronizeExpenses()
            notesRepo.synchronizeNotes()
        }
    }
}

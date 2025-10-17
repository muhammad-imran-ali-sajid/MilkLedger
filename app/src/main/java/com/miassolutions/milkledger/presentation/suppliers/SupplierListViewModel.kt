package com.miassolutions.milkledger.presentation.suppliers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miassolutions.milkledger.data.mapper.toDomain
import com.miassolutions.milkledger.data.mapper.toEntity
import com.miassolutions.milkledger.data.repository.CustomerRepository
import com.miassolutions.milkledger.data.repository.SupplierRepository
import com.miassolutions.milkledger.domain.model.Customer
import com.miassolutions.milkledger.domain.model.Supplier
import com.miassolutions.milkledger.presentation.customers.CustomerUiEvent
import com.miassolutions.milkledger.presentation.customers.CustomerUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SupplierListViewModel @Inject constructor(
    private val repository: SupplierRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SupplierUiState())
    val uiState = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<SupplierUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    init {
        observeSuppliers()
    }

    private fun observeSuppliers() {
        viewModelScope.launch {
            repository.getAllSuppliers()
                .map { list -> list.map { it.toDomain() } } // Convert to domain model
                .collect { suppliers ->
                    _uiState.value = _uiState.value.copy(suppliers = suppliers)
                }
        }
    }

    fun onAddSupplierClick() {
        viewModelScope.launch {
            _uiEvent.emit(SupplierUiEvent.ShowSupplierForm)
        }
    }

    fun saveSupplier(supplier: Supplier) {
        viewModelScope.launch {
            try {
                repository.insertSupplier(supplier.toEntity())
                _uiEvent.emit(SupplierUiEvent.ShowMessage("Supplier saved"))
            } catch (e: Exception) {
                _uiEvent.emit(SupplierUiEvent.ShowMessage("Error saving supplier"))
            }
        }
    }


    fun deleteSupplier(supplier: Supplier) {
        viewModelScope.launch {
            try {
                repository.deleteSupplier(supplier.toEntity())
                _uiEvent.emit(SupplierUiEvent.ShowMessage("Supplier deleted"))
            } catch (e: Exception) {
                _uiEvent.emit(SupplierUiEvent.ShowMessage("Error deleting supplier"))
            }
        }
    }


}

package com.miassolutions.milkledger.presentation.customer.customers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miassolutions.milkledger.data.mapper.toDomain
import com.miassolutions.milkledger.data.mapper.toEntity
import com.miassolutions.milkledger.data.repository.CustomerRepository
import com.miassolutions.milkledger.domain.model.Customer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CustomerListViewModel @Inject constructor(
    private val repository: CustomerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CustomerUiState())
    val uiState = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<CustomerUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    init {
        observeCustomers()
    }

    private fun observeCustomers() {
        viewModelScope.launch {
            repository.getAllCustomers()
                .map { list -> list.map { it.toDomain() } } // Convert to domain model
                .collect { customers ->
                    _uiState.value = _uiState.value.copy(customers = customers)
                }
        }
    }

    fun onAddCustomerClick() {
        viewModelScope.launch {
            _uiEvent.emit(CustomerUiEvent.ShowCustomerForm)
        }
    }



    fun saveCustomer(customer: Customer) {
        viewModelScope.launch {
            try {
                repository.insertCustomer(customer.toEntity())
                _uiEvent.emit(CustomerUiEvent.ShowMessage("Customer saved"))
            } catch (e: Exception) {
                _uiEvent.emit(CustomerUiEvent.ShowMessage("Error saving customer"))
            }
        }
    }

    fun deleteCustomer(customer: Customer){
        viewModelScope.launch {
            try {
                repository.deleteCustomer(customer.toEntity())
                _uiEvent.emit(CustomerUiEvent.ShowMessage("Customer deleted"))
            } catch (e:Exception){
                _uiEvent.emit(CustomerUiEvent.ShowMessage("Error deleting customer"))
            }
        }
    }


}

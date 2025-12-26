package com.miassolutions.milkledger.presentation.customer.customers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miassolutions.milkledger.data.repository.CustomerRepository
import com.miassolutions.milkledger.domain.model.Customer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
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

    private var allCustomers: List<Customer> = emptyList()

    init {
        observeCustomers()
    }

    private fun observeCustomers() {
        viewModelScope.launch {
            repository.getAllCustomers()
                .collect { customers ->
                    allCustomers = customers
                    _uiState.value = _uiState.value.copy(
                        customers = customers,
                        displayedCustomers = customers
                    )
                }
        }
    }

    fun onAddCustomerClick() {
        viewModelScope.launch {
            _uiEvent.emit(CustomerUiEvent.ShowCustomerForm)
        }
    }

    fun deleteCustomer(customer: Customer) {
        viewModelScope.launch {
            repository.deleteCustomer(customer.id)
            _uiEvent.emit(CustomerUiEvent.ShowMessage("Customer deleted"))
        }
    }
}


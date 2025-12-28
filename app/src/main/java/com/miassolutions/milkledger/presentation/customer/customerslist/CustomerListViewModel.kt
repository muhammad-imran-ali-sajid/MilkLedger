package com.miassolutions.milkledger.presentation.customer.customerslist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miassolutions.milkledger.data.repository.CustomerRepository
import com.miassolutions.milkledger.presentation.customer.mapper.toUI
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CustomerListViewModel @Inject constructor(
    private val repository: CustomerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CustomerUiState())
    val uiState = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<CustomerUiEffect>()
    val uiEffect = _uiEffect.asSharedFlow()

    private fun emitEffect(effect: CustomerUiEffect) {
        viewModelScope.launch { _uiEffect.emit(effect) }
    }

    fun onEvent(event: CustomerUiEvent) {
        when (event) {
            CustomerUiEvent.OnAddCustomerClick -> {
                emitEffect(CustomerUiEffect.NavigateToAddCustomer)
            }

            CustomerUiEvent.OnRetryClick -> {
                loadCustomers()
            }

            is CustomerUiEvent.OnDeleteCustomer -> {
                deleteCustomer(event.customerId)
            }

            is CustomerUiEvent.OnSearchQueryChange -> {
                _uiState.update { it.copy(searchQuery = event.query) }
            }
        }
    }


    init {
        loadCustomers()
    }

    private fun loadCustomers() {
        viewModelScope.launch {
            repository.getAllCustomers()
                .onStart {
                    _uiState.update { it.copy(isLoading = true, error = null) }
                }
                .catch {
                    _uiState.update {
                        it.copy(isLoading = false, error = it.error)
                    }
                    _uiEffect.emit(
                        CustomerUiEffect.ShowMessage("Failed to load customers")
                    )
                }
                .collect { customers ->

                    val customersUI = customers.map { it.toUI() }

                    _uiState.update {
                        it.copy(customers = customersUI, isLoading = false)
                    }

                }


        }
    }


    private fun deleteCustomer(customerId: String) {
        viewModelScope.launch {
            repository.deleteCustomer(customerId)
            _uiEffect.emit(CustomerUiEffect.ShowMessage("Customer deleted"))
        }
    }
}


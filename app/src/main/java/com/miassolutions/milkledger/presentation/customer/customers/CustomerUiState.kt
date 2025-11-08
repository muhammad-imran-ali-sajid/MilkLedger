package com.miassolutions.milkledger.presentation.customer.customers

import com.miassolutions.milkledger.domain.model.Customer
import com.miassolutions.milkledger.domain.model.Supplier

data class CustomerUiState(
    val customers: List<Customer> = emptyList(),
    val displayedCustomers: List<Customer> = emptyList(),
    val isLoading: Boolean = false
)


sealed class CustomerUiEvent {
    data object ShowCustomerForm : CustomerUiEvent()

    data class ShowMessage(val message: String) : CustomerUiEvent()
}

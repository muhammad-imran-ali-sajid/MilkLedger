package com.miassolutions.milkledger.presentation.customer.customers.ui

import com.miassolutions.milkledger.domain.model.Customer
import com.miassolutions.milkledger.presentation.customer.customers.model.CustomerUi

data class CustomerUiState(
    val customers: List<CustomerUi> = emptyList(),
    val displayedCustomers: List<CustomerUi> = emptyList(),
    val isLoading: Boolean = false
)


sealed class CustomerUiEvent {
    data object ShowCustomerForm : CustomerUiEvent()

    data class ShowMessage(val message: String) : CustomerUiEvent()
}

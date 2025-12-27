package com.miassolutions.milkledger.presentation.customer.customerslist

import com.miassolutions.milkledger.presentation.customer.model.CustomerUi

data class CustomerUiState(
    val customers: List<CustomerUi> = emptyList(),
    val displayedCustomers: List<CustomerUi> = emptyList(),
    val isLoading: Boolean = false
)


sealed interface CustomerUiEvent {
    data object ShowAddCustomerForm : CustomerUiEvent
    data class ShowMessage(val message: String) : CustomerUiEvent
}

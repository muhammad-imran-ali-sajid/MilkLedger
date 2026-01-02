package com.miassolutions.milkledger.features.customer.ui.list

import com.miassolutions.milkledger.features.customer.ui.model.CustomerUi


data class CustomerUiState(
    val customers: List<CustomerUi> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
) {
    val isEmpty: Boolean
        get() = !isLoading && customers.isEmpty()

    val visibleCustomers: List<CustomerUi>
        get() = if (searchQuery.isBlank()) {
            customers
        } else {
            customers.filter { it.name.contains(searchQuery, ignoreCase = true) }
        }
}

sealed interface CustomerUiEffect {
    data object NavigateToAddCustomer : CustomerUiEffect
    data class OpenOptionDialog(val customerId: String) : CustomerUiEffect
    data class ShowMessage(val message: String) : CustomerUiEffect
}


sealed interface CustomerUiEvent {

    data object OnAddCustomerClick : CustomerUiEvent
    data class OnCustomerItemClicked(val customerId: String) : CustomerUiEvent
    data class OnDeleteCustomer(val customerId: String) : CustomerUiEvent
    data class OnSearchQueryChange(val query: String) : CustomerUiEvent
    data object OnRetryClick : CustomerUiEvent
}



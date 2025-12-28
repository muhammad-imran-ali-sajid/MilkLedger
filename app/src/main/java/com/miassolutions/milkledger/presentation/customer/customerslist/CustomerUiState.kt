package com.miassolutions.milkledger.presentation.customer.customerslist

import com.miassolutions.milkledger.presentation.customer.model.CustomerUi

data class CustomerUiState(
    val customers: List<CustomerUi> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
) {
    val isEmpty: Boolean
        get() = customers.isEmpty() && !isLoading

    val visibleCustomers: List<CustomerUi>
        get() = if (searchQuery.isBlank()) {
            customers
        } else {
            customers.filter { it.name.contains(searchQuery, ignoreCase = true) }
        }
}



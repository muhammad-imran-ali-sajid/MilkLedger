package com.miassolutions.milkledger.presentation.customerandsales.customer.customerslist

sealed interface CustomerUiEvent {

    data object OnAddCustomerClick : CustomerUiEvent
    data class OnCustomerItemClicked(val customerId: String) : CustomerUiEvent
    data class OnDeleteCustomer(val customerId: String) : CustomerUiEvent
    data class OnSearchQueryChange(val query: String) : CustomerUiEvent
    data object OnRetryClick : CustomerUiEvent
}
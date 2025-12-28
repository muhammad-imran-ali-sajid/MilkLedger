package com.miassolutions.milkledger.presentation.customerandsales.customer.customerslist


sealed interface CustomerUiEffect {
    data object NavigateToAddCustomer : CustomerUiEffect
    data class OpenOptionDialog(val customerId: String): CustomerUiEffect
    data class ShowMessage(val message: String) : CustomerUiEffect
}
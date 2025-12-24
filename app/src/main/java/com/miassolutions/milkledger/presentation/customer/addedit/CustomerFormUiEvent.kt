package com.miassolutions.milkledger.presentation.customer.addedit

import com.miassolutions.milkledger.domain.model.Customer


sealed class CustomerFormUiEvent {
    data class SaveCustomer(val customer: Customer) : CustomerFormUiEvent()
    object Dismiss : CustomerFormUiEvent()
}
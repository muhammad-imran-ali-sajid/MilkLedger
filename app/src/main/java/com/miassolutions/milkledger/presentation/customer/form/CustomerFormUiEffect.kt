package com.miassolutions.milkledger.presentation.customer.form

sealed interface CustomerFormUiEffect {

    data object Dismiss : CustomerFormUiEffect
    data class ShowMessage(val message: String) : CustomerFormUiEffect
}
package com.miassolutions.milkledger.presentation.customerandsales.customer.form


sealed interface CustomerFormUiEvent {

    data class OnNameChanged(val value: String) : CustomerFormUiEvent
    data class OnRateChanged(val value: String) : CustomerFormUiEvent
    data class OnPositionChanged(val value: String) : CustomerFormUiEvent
    data class OnAdvanceAmountChanged(val value: String) : CustomerFormUiEvent

    data object OnSaveClicked : CustomerFormUiEvent

}
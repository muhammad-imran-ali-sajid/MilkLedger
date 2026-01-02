package com.miassolutions.milkledger.features.customer.ui.form

data class CustomerFormUiState(
    val name: String = "",
    val rate: String = "",
    val position: String = "",
    val advanceAmount: String = "",

    val isEdit: Boolean = false,
    val isSaving: Boolean = false,


    val nameError: String? = null,
    val rateError: String? = null,
    val positionError: String? = null
)

sealed interface CustomerFormUiEvent {

    data class OnNameChanged(val value: String) : CustomerFormUiEvent
    data class OnRateChanged(val value: String) : CustomerFormUiEvent
    data class OnPositionChanged(val value: String) : CustomerFormUiEvent
    data class OnAdvanceAmountChanged(val value: String) : CustomerFormUiEvent

    data object OnSaveClicked : CustomerFormUiEvent

}

sealed interface CustomerFormUiEffect {

    data object Dismiss : CustomerFormUiEffect
    data class ShowMessage(val message: String) : CustomerFormUiEffect
}
package com.miassolutions.milkledger.features.supplier.ui.form


data class SupplierFormUiState(
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

sealed interface SupplierFormUiEffect {
    data object Dismiss : SupplierFormUiEffect
    data class ShowMessage(val message: String) : SupplierFormUiEffect
}


sealed interface SupplierFormUiEvent {
    data class OnNameChanged(val name : String): SupplierFormUiEvent
    data class OnRateChanged(val rate: String): SupplierFormUiEvent
    data class OnPositionChanged(val position: String) : SupplierFormUiEvent

    data class OnAdvanceAmountChanged(val amount: String) : SupplierFormUiEvent

    data object OnSaveClicked : SupplierFormUiEvent
}



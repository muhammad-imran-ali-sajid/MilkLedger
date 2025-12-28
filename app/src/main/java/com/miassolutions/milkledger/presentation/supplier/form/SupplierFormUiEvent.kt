package com.miassolutions.milkledger.presentation.supplier.form

sealed interface SupplierFormUiEvent {
    data class OnNameChanged(val name : String): SupplierFormUiEvent
    data class OnRateChanged(val rate: String): SupplierFormUiEvent
    data class OnPositionChanged(val position: String) : SupplierFormUiEvent

    data class OnAdvanceAmountChanged(val amount: String) : SupplierFormUiEvent

    data object OnSaveClicked : SupplierFormUiEvent
}
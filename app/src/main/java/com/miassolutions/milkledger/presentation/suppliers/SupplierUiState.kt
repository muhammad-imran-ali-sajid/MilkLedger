package com.miassolutions.milkledger.presentation.suppliers

import com.miassolutions.milkledger.domain.model.Supplier

data class SupplierUiState(
    val suppliers: List<Supplier> = emptyList(),
    val isLoading: Boolean = false
)


sealed class SupplierUiEvent {
    data object ShowSupplierForm : SupplierUiEvent()
    data class ShowMessage(val message: String) : SupplierUiEvent()
}
package com.miassolutions.milkledger.features.supplier.ui.suppliers

import com.miassolutions.milkledger.features.supplier.ui.model.SupplierUi


data class SupplierListUiState(
    val suppliers: List<SupplierUi> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
) {
    val isEmpty: Boolean
        get() = !isLoading && suppliers.isEmpty()

    val visibleSupplier : List<SupplierUi>
        get() = if (searchQuery.isBlank()) {
            suppliers
        } else {
            suppliers.filter { it.name.contains(searchQuery, ignoreCase = true) }
        }
}

sealed interface SupplierListUiEffect {
    data object NavToAddSupplierForm : SupplierListUiEffect
    data class OpenOptionDialog(val supplierId: String) : SupplierListUiEffect
    data class ShowMessage(val message: String) : SupplierListUiEffect
}



sealed interface SupplierListUiEvent {
    data object OnAddSupplierClick : SupplierListUiEvent
    data class OnSupplierItemClick(val supplierId: String) : SupplierListUiEvent
    data class OnDeleteSupplierClick(val supplierId: String) : SupplierListUiEvent
    data class OnSearchQueryChange(val name: String) : SupplierListUiEvent
    data object OnRetryClick : SupplierListUiEvent


}



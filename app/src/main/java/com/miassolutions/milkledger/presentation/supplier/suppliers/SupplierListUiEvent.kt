package com.miassolutions.milkledger.presentation.supplier.suppliers

sealed interface SupplierListUiEvent {
    data object OnAddSupplierClick : SupplierListUiEvent
    data class OnDeleteSupplierClick(val supplierId: String) : SupplierListUiEvent
    data class OnSearchQueryChange(val name: String) : SupplierListUiEvent

    data object OnRetryClick : SupplierListUiEvent


}
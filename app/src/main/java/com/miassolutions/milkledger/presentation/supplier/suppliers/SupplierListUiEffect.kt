package com.miassolutions.milkledger.presentation.supplier.suppliers

sealed interface SupplierListUiEffect {
    data object NavToSupplierForm : SupplierListUiEffect

    data class ShowMessage(val message: String) : SupplierListUiEffect
}
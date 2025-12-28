package com.miassolutions.milkledger.presentation.supplier.suppliers

sealed interface SupplierListUiEffect {
    data object NavToAddSupplierForm : SupplierListUiEffect

    data class OpenOptionDialog(val supplierId: String) : SupplierListUiEffect


    data class ShowMessage(val message: String) : SupplierListUiEffect
}
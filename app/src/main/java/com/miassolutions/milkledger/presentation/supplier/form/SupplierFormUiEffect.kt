package com.miassolutions.milkledger.presentation.supplier.form

sealed interface SupplierFormUiEffect {
    data object Dismiss : SupplierFormUiEffect
    data class ShowMessage(val message: String) : SupplierFormUiEffect
}
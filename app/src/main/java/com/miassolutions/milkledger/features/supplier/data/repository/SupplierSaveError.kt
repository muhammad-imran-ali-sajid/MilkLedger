package com.miassolutions.milkledger.features.supplier.data.repository

sealed class SupplierSaveError : Throwable() {
    data class SortOrderAlreadyExists(val sortOrder: Int) : SupplierSaveError()
}
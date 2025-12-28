package com.miassolutions.milkledger.data.util

sealed class SupplierSaveError : Throwable() {
    data class SortOrderAlreadyExists(val sortOrder: Int) : SupplierSaveError()
}

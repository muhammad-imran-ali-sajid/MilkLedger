package com.miassolutions.milkledger.data.util

sealed class CustomerSaveError : Throwable() {
    data class SortOrderAlreadyExists(val sortOrder: Int) : CustomerSaveError()
}

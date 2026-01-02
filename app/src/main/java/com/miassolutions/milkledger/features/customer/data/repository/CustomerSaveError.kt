package com.miassolutions.milkledger.features.customer.data.repository

sealed class CustomerSaveError : Throwable() {
    data class SortOrderAlreadyExists(val sortOrder: Int) : CustomerSaveError()
}
package com.miassolutions.milkledger.presentation.supplier.form


data class SupplierFormUiState(
    val name: String = "",
    val rate: String = "",
    val position: String = "",
    val advanceAmount: String = "",

    val isEdit: Boolean = false,
    val isSaving: Boolean = false,


    val nameError: String? = null,
    val rateError: String? = null,
    val positionError: String? = null
)



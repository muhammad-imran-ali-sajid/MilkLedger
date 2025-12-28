package com.miassolutions.milkledger.presentation.supplier.suppliers

import com.miassolutions.milkledger.domain.model.Supplier
import com.miassolutions.milkledger.presentation.supplier.model.SupplierUi

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



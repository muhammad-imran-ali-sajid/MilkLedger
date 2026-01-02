package com.miassolutions.milkledger.features.cashflow

import java.time.LocalDate

// ---------- UiEvents ----------
sealed class FilterUiEvent {
    object Dismiss : FilterUiEvent()
    data class ApplyFilter(
        val fromDate: LocalDate?,
        val toDate: LocalDate?,
        val selectedType: FilterType
    ) : FilterUiEvent()
    object ResetFilter : FilterUiEvent()
}


// ---------- Enum for filter types ----------
enum class FilterType {
    CUSTOMER, SUPPLIER, ALL
}

// ---------- UiState ----------
data class FilterUiState(
    val fromDate: LocalDate? = null,
    val toDate: LocalDate? = null,
    val selectedType: FilterType = FilterType.ALL,
    val isApplyEnabled: Boolean = false
)
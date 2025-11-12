package com.miassolutions.milkledger.presentation.supplier.supplierdetail

import java.time.LocalDate

data class SupplierDetailUiState(
    val selectedSupplierId: String? = null,
    val supplierName: String = "",
    val supplierDetailList: List<SupplierDetailModel> = emptyList(),
    val filteredList: List<SupplierDetailModel> = emptyList(),
    val selectedStartDate: LocalDate? = null,
    val selectedEndDate: LocalDate? = null,
    val isInitialLoadComplete: Boolean = false,
    val summary: SupplierSummary = SupplierSummary()
)

data class SupplierSummary(
    val summaryPeriod: String = "",
    val totalMilk: String = "",
    val avgFat : String = "",
    val avgLr : String = "",
    val totalTS: String = "",
    val totalPrice: String = "",
    val paidAmount: String = "",
    val balance: Double = 0.0
)

sealed class SupplierUiEvent {
    data class ChangeDateRange(val start: LocalDate, val end: LocalDate) : SupplierUiEvent()
}
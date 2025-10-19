package com.miassolutions.milkledger.presentation.supplier.supplierdetail

import com.miassolutions.milkledger.core.ui.datesort.DateRangeType
import com.miassolutions.milkledger.core.ui.sort.FilterOptions
import java.time.LocalDate

data class SupplierDetailUiState(
    val selectedSupplierId: String? = null,
    val supplierName: String = "",
    val supplierDetailList: List<SupplierDetailModel> = emptyList(),
    val filteredList: List<SupplierDetailModel> = emptyList(), //  filtered copy
    val currentFilter: FilterOptions = FilterOptions(),//  remember current filter
    val dateRangeType: DateRangeType = DateRangeType.ALL,
    val selectedStartDate: LocalDate? = null,
    val selectedEndDate: LocalDate? = null
)


sealed class SupplierUiEvent {
    data class ApplyFilter(val filter: FilterOptions) : SupplierUiEvent()
    data class ChangeDateRange(val rangeType: DateRangeType) : SupplierUiEvent()
    data object NextButton : SupplierUiEvent()
    data object PrevButton : SupplierUiEvent()
}
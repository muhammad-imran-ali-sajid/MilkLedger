package com.miassolutions.milkledger.presentation.details

import com.miassolutions.milkledger.core.ui.datesort.DateRangeType
import com.miassolutions.milkledger.core.ui.sort.FilterOptions

data class CustomerDetailUiState(
    val selectedCustomerId: String? = null,
    val customerName: String = "",
    val customerDetailList: List<CustomerDetailModel> = emptyList(),
    val filteredList: List<CustomerDetailModel> = emptyList(), //  filtered copy
    val currentFilter: FilterOptions = FilterOptions() ,//  remember current filter
    val dateRangeType: DateRangeType = DateRangeType.ALL
)


sealed class CustomerUiEvent {
    data class ApplyFilter(val filter: FilterOptions) : CustomerUiEvent()
    data class ChangeDateRange(val rangeType: DateRangeType) : CustomerUiEvent()
}

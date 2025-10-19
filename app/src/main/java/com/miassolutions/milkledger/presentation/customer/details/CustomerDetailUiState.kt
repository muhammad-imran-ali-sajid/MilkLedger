package com.miassolutions.milkledger.presentation.customer.details

import com.miassolutions.milkledger.core.ui.datesort.DateRangeType
import com.miassolutions.milkledger.core.ui.sort.FilterOptions
import java.time.LocalDate

data class CustomerDetailUiState(
    val selectedCustomerId: String? = null,
    val customerName: String = "",
    val customerDetailList: List<CustomerDetailModel> = emptyList(),
    val filteredList: List<CustomerDetailModel> = emptyList(), //  filtered copy
    val currentFilter: FilterOptions = FilterOptions(),//  remember current filter
    val dateRangeType: DateRangeType = DateRangeType.ALL,
    val selectedStartDate: LocalDate? = null,
    val selectedEndDate: LocalDate? = null
)


sealed class CustomerUiEvent {
    data class ApplyFilter(val filter: FilterOptions) : CustomerUiEvent()
    data class ChangeDateRange(val rangeType: DateRangeType) : CustomerUiEvent()
    data object NextButton : CustomerUiEvent()
    data object PrevButton : CustomerUiEvent()
}

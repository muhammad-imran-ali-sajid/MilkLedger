package com.miassolutions.milkledger.presentation.customer.customerdetail

import java.time.LocalDate

data class CustomerDetailUiState(
    val selectedCustomerId: String? = null,
    val customerName: String = "",
    val isRateChanged: Boolean = false,
    val customerDetailList: List<CustomerDetailModel> = emptyList(),
    val filteredList: List<CustomerDetailModel> = emptyList(), //  filtered copy
//    val currentFilter: FilterOptions = FilterOptions(),//  remember current filter
    val selectedStartDate: LocalDate? = null,
    val selectedEndDate: LocalDate? = null,
    val summary: CustomerSummary = CustomerSummary()
)


sealed class CustomerUiEvent {
    data class ChangeDateRange(val start: LocalDate, val end: LocalDate) : CustomerUiEvent()
}

data class CustomerSummary(
    val summaryPeriod: String = "",
    val totalMilk: String = "",
    val totalDeduction: String = "",
    val totalPrice: String = "",
    val paidAmount: String = "",
    val balance: Double = 0.0
)

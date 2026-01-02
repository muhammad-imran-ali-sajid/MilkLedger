package com.miassolutions.milkledger.features.profitwithdrawal

import java.time.LocalDate

data class ProfitUiState(
    val profitList: List<ProfitListModel> = emptyList(),
    val filteredList: List<ProfitListModel> = emptyList(),
    val grossProfit: Double = 0.0,
    val netProfit: Double = 0.0,
    val totalReceived: Double = 0.0,
    val remainingProfit: Double = 0.0,
    val periodLabel: String = "",
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)



package com.miassolutions.milkledger.presentation.profit

import com.miassolutions.milkledger.data.local.entities.ProfitEntity
import com.miassolutions.milkledger.domain.model.Profit
import java.time.LocalDate

data class ProfitUiState(
    val profitList: List<ProfitListModel> = emptyList(),
    val filteredList: List<ProfitListModel> = emptyList(),
    val netBusinessProfit: Double = 0.0,
    val netProfitAfterPersonalExpenses: Double = 0.0,
    val totalReceived: Double = 0.0,
    val remainingProfit: Double = 0.0,
    val periodLabel: String = "",
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)



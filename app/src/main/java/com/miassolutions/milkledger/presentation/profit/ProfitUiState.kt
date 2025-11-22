package com.miassolutions.milkledger.presentation.profit

import com.miassolutions.milkledger.data.local.entities.ProfitEntity
import com.miassolutions.milkledger.domain.model.Profit
import java.time.LocalDate

data class ProfitUiState(
    val profitList: List<Profit> = emptyList(),
    val filteredList: List<Profit> = emptyList(),
    val netProfit: Double = 0.0,
    val totalReceived: Double = 0.0,
    val remainingProfit : Double = 0.0,
    val selectedStartDate: LocalDate? = null,
    val selectedEndDate: LocalDate? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
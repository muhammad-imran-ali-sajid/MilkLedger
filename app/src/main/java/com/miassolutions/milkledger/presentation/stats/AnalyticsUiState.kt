package com.miassolutions.milkledger.presentation.stats

import java.time.LocalDate

data class AnalyticsUiState(
    val period: String = "",
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val milkPurchase: Double = 0.0,
    val milkSold: Double = 0.0,
    val avgSP: Double ?= 0.0,
    val avgCP: Double? = 0.0,
    val difference: Double? = 0.0,
    val salesTotal: Double = 0.0,
    val fixedExpense: Double = 0.0,
    val personalExpense : Double =0.0,
    val purchaseTotal: Double = 0.0,
    val profit: Double = 0.0,
    val profitAfter : Double = 0.0,
    val avgFat: Double = 0.0,
    val avgLr: Double = 0.0,
    val totalTs: Double = 0.0,
    val totalMilkWithFatAndLr: Double = 0.0
)


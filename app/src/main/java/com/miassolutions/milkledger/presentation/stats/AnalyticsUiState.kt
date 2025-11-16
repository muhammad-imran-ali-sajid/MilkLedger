package com.miassolutions.milkledger.presentation.stats

import java.time.LocalDate

data class AnalyticsUiState(
    val period: String = "",
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val milkPurchase: Double = 0.0,
    val milkSold: Double = 0.0,
    val salesTotal: Double = 0.0,
    val expensesTotal: Double = 0.0,
    val purchaseTotal: Double = 0.0,
    val profit: Double = 0.0,
    val avgFat: Double = 0.0,
    val avgLr: Double = 0.0,
    val totalTs: Double = 0.0,
    val totalMilkWithFatAndLr : Double = 0.0
)


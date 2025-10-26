package com.miassolutions.milkledger.presentation.stats

import java.time.LocalDate

data class AnalyticsUiState(
    val period: String = "This Week",
    val milkPurchase : Double = 0.0,
    val milkSold : Double = 0.0,
    val salesTotal: Double = 0.0,
    val expensesTotal : Double = 0.0,
    val purchaseTotal: Double = 0.0,
    val profit: Double = 0.0,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
)


// ---------- ENUM ----------
enum class AnalyticsPeriod {
    WEEKLY, MONTHLY, YEARLY, CUSTOM
}
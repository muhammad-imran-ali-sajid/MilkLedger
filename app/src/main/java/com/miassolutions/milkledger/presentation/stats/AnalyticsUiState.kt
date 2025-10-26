package com.miassolutions.milkledger.presentation.stats

import java.time.LocalDate

data class AnalyticsUiState(
    val period: String = "This Week",
    val salesTotal: Double = 0.0,
    val purchaseTotal: Double = 0.0,
    val profit: Double = 0.0,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val isLoading: Boolean = false
)


// ---------- ENUM ----------
enum class AnalyticsPeriod {
    WEEKLY, MONTHLY, YEARLY, CUSTOM
}
package com.miassolutions.milkledger.presentation.stats

data class AnalyticsUiState(
    val period: String = "",
    val salesTotal: Double = 0.0,
    val purchaseTotal: Double = 0.0,
    val profit: Double = 0.0
)
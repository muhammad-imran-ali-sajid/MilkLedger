package com.miassolutions.milkledger.presentation.dashboard

import java.time.LocalDate

data class DashboardUiState(
    val period: String = "This Week",
    val milkPurchase: Double = 0.0,
    val milkSold: Double = 0.0,
    val salesTotal: Double = 0.0,
    val expensesTotal: Double = 0.0,
    val purchaseTotal: Double = 0.0,
    val profit: Double = 0.0,
    val dashboardSummaryPdfUiState: DashboardSummaryPdf = DashboardSummaryPdf(),
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
)


data class DashboardSummaryPdf(

    // Date Range
    val dateRange: String = "",

    // Payment Overview
    val totalPurchasePrice: String = "",
    val totalSalePrice: String = "",
    val businessExpenses: String = "",
    val profit: String = "",
    val personalExpenses: String = "",
    val remainingProfit: String = "",

    // Milk Overview
    val milkPurchased: String = "",
    val milkSold: String = "",
    val quantityDifference: String = "",
    val averageFat: String = "",
    val averageLr: String = "",
    val averageSalePrice: String = "",
    val averageCostPrice: String = "",
    val averagePriceDifference: String = ""
)

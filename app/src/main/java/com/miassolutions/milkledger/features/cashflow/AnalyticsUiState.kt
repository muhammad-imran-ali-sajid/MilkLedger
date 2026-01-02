package com.miassolutions.milkledger.features.cashflow

import com.miassolutions.milkledger.features.dashboard.DashboardSummaryPdf
import com.miassolutions.milkledger.utils.extensions.toPriceStr
import com.miassolutions.milkledger.utils.extensions.toRoundedStr
import java.time.LocalDate

data class AnalyticsUiState(
    val period: String = "",
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val milkPurchase: Double = 0.0,
    val milkSold: Double = 0.0,
    val avgSP: Double? = 0.0,
    val avgCP: Double? = 0.0,
    val difference: Double? = 0.0,
    val salesTotal: Double = 0.0,
    val fixedExpense: Double = 0.0,
    val personalExpense: Double = 0.0,
    val purchaseTotal: Double = 0.0,
    val profit: Double = 0.0,
    val profitAfter: Double = 0.0,
    val avgFat: Double = 0.0,
    val avgLr: Double = 0.0,
    val totalTs: Double = 0.0,
    val periodLabel: String = "",
    val totalMilkWithFatAndLr: Double = 0.0
)

fun AnalyticsUiState.toPdfSummary(): DashboardSummaryPdf = with(this) {
    return DashboardSummaryPdf(
        dateRange = period,
        totalPurchasePrice = purchaseTotal.toPriceStr(),
        totalSalePrice = salesTotal.toPriceStr(),
        businessExpenses = fixedExpense.toPriceStr(),
        profit = profit.toPriceStr(),
        personalExpenses = personalExpense.toPriceStr(),
        remainingProfit = profitAfter.toPriceStr(),
        milkPurchased = milkPurchase.toRoundedStr(),
        milkSold = milkSold.toRoundedStr(),
        quantityDifference = (milkSold - milkPurchase).toRoundedStr(),
        averageFat = avgFat.toRoundedStr(),
        averageLr = avgLr.toRoundedStr(),
        averageSalePrice = avgSP?.toRoundedStr() ?: "",
        averageCostPrice = avgCP?.toRoundedStr() ?: "",
        averagePriceDifference = difference?.toRoundedStr() ?: ""
    )
}


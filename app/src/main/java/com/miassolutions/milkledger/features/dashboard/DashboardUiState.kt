package com.miassolutions.milkledger.features.dashboard

import com.miassolutions.milkledger.utils.customviews.DateFilterView
import java.time.LocalDate


// 1. STATE (Data jo UI par show hoga)
data class DashboardUiState(
    val isLoading: Boolean = false,

    // 🔵 Report Range (Dashboard stats / charts)
    val reportStartDate: LocalDate = LocalDate.now(),
    val reportEndDate: LocalDate = LocalDate.now(),

    val selectedDate: LocalDate = LocalDate.now(),

    val filterMode: DateFilterView.FilterMode = DateFilterView.FilterMode.DAY,

    // Financials
    val totalPurchases: Long = 0,
    val totalSales: Long = 0,
    val totalExpenses: Long = 0,
    val grossProfit: Long = 0,

    // Milk Quantities
    val milkPurchasedQty: Double = 0.0,
    val milkSoldQty: Double = 0.0,
    val qtyDiff: Double = 0.0,

    // Averages
    val avgPurchasePrice: Double = 0.0,
    val avgSalePrice: Double = 0.0,
    val avgPriceDiff: Double = 0.0,

    // Quality
    val avgFat: Double = 0.0,
    val avgLr: Double = 0.0,
    val qualityVolume: Double = 0.0,
    val totalTs: Double = 0.0
)

// 2. EVENTS (User ke actions)
sealed interface DashboardUiEvent {

    // Report filter change
    data class OnDateFilterChanged(
        val startDate: LocalDate,
        val endDate: LocalDate,
        val selectedSingleDate: LocalDate,
        val mode: DateFilterView.FilterMode
    ) : DashboardUiEvent

    // Click actions


    object OnNotesClicked : DashboardUiEvent
    object OnCashFlowClicked : DashboardUiEvent
    object OnWalletClicked : DashboardUiEvent
    object OnPurchaseClicked : DashboardUiEvent
    object OnSaleClicked : DashboardUiEvent
    object OnExpenseClicked : DashboardUiEvent
}


// 3. EFFECTS (Navigation wagera)
sealed interface DashboardUiEffect {

    object NavigateToNotes : DashboardUiEffect

    data class NavigateToCashFlow(
        val date: LocalDate
    ) : DashboardUiEffect

    data class NavigateToWallet(
        val date: LocalDate
    ) : DashboardUiEffect

    data class NavigateToPurchase(
        val date: LocalDate
    ) : DashboardUiEffect

    data class NavigateToSale(
        val date: LocalDate
    ) : DashboardUiEffect

    data class NavigateToExpense(
        val date: LocalDate
    ) : DashboardUiEffect
}

package com.miassolutions.milkledger.features.dashboard


// 1. STATE (Data jo UI par show hoga)
data class DashboardUiState(
    val isLoading: Boolean = false,

    // Financials (Paisa)
    val totalPurchases: Long = 0,
    val totalSales: Long = 0,
    val totalExpenses: Long = 0,
    val grossProfit: Long = 0,

    // Milk Quantities
    val milkPurchasedQty: Double = 0.0,
    val milkSoldQty: Double = 0.0,
    val qtyDiff: Double = 0.0, // Sold - Purchased

    // Averages
    val avgPurchasePrice: Double = 0.0,
    val avgSalePrice: Double = 0.0,
    val avgPriceDiff: Double = 0.0,

    // Quality
    val avgFat: Double = 0.0,
    val avgLr: Double = 0.0,
    val totalTs: Double = 0.0
)

// 2. EVENTS (User ke actions)
sealed class DashboardUiEvent {
    data class OnDateFilterChanged(val start: Long, val end: Long) : DashboardUiEvent()
    object OnNotesClicked : DashboardUiEvent()
    object OnCashFlowClicked : DashboardUiEvent()
}

// 3. EFFECTS (Navigation wagera)
sealed class DashboardUiEffect {
    object NavigateToNotes : DashboardUiEffect()
    object NavigateToCashFlow : DashboardUiEffect()
}
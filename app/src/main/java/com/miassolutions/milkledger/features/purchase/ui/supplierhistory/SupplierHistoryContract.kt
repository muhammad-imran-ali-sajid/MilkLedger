package com.miassolutions.milkledger.features.purchase.ui.supplierhistory


import com.miassolutions.milkledger.features.purchase.model.MilkPurchaseUiModel

data class SupplierHistoryUiState(
    val isLoading: Boolean = true,
    val supplierName: String = "",
    val transactions: List<MilkPurchaseUiModel> = emptyList(),

    // Date Filter Text (e.g., "This Month")
    val dateRangeText: String = "All History",
    val startDate: Long = 0L,
    val endDate: Long = Long.MAX_VALUE,

    // Summary Bottom Sheet
    val summaryMilk: Double = 0.0,
    val summaryAmount: Long = 0,
    val summaryPaid: Long = 0,
    val currentTotalBalance: Long = 0
)

sealed class SupplierHistoryUiEvent {
    data class OnDateFilterChanged(val start: Long, val end: Long, val label: String) : SupplierHistoryUiEvent()
    data class OnTransactionClick(val purchaseId: String) : SupplierHistoryUiEvent()
    object OnBackClick : SupplierHistoryUiEvent()
}

sealed class SupplierHistoryUiEffect {
    data class NavigateToEditPurchase(val purchaseId: String) : SupplierHistoryUiEffect()
    object NavigateBack : SupplierHistoryUiEffect()
    data class ShowSnackbar(val message: String) : SupplierHistoryUiEffect()
}
package com.miassolutions.milkledger.features.purchase.ui.supplierhistory


import com.miassolutions.milkledger.features.purchase.model.MilkPurchaseUiModel
import com.miassolutions.milkledger.features.purchase.model.PurchaseSummary

data class SupplierHistoryUiState(
    val isLoading: Boolean = true,
    val supplierName: String = "",

    val transactions: List<MilkPurchaseUiModel> = emptyList(),
    val displayedTransactions: List<MilkPurchaseUiModel> = emptyList(), // Screen par show hone wali list
    val searchQuery: String = "",
    
    val dateRangeText: String = "All History",
    val summary: PurchaseSummary = PurchaseSummary()

)

sealed class SupplierHistoryUiEvent {
    data class OnDateFilterChanged(val start: Long, val end: Long, val label: String) :
        SupplierHistoryUiEvent()
    data class OnSearchQueryChanged(val query: String) : SupplierHistoryUiEvent()
    object OnBackClick : SupplierHistoryUiEvent()
}

sealed class SupplierHistoryUiEffect {
    object NavigateBack : SupplierHistoryUiEffect()
}
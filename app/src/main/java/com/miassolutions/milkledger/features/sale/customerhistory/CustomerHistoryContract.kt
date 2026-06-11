package com.miassolutions.milkledger.features.sale.customerhistory

import com.miassolutions.milkledger.features.purchase.model.SaleSummary
import com.miassolutions.milkledger.features.sale.model.MilkSaleUiModel

// ==========================================
// 1️⃣ UI STATE (Screen par kya dikhana hai)
// ==========================================
data class CustomerHistoryUiState(
    // Loading State
    val isLoading: Boolean = true,

    // Customer Info
    val customerId: String = "",
    val customerName: String = "",

    // Data List
    val transactions: List<MilkSaleUiModel> = emptyList(),
    
    // 🔥 Search hone ke baad wala data yahan aayega (Adapter ko ye list deni hai)
    val displayedTransactions: List<MilkSaleUiModel> = emptyList(),
    
    val searchQuery: String = "", // Current search text

    val summary: SaleSummary = SaleSummary(),

    // Header Info (e.g. "All History" or "01 Jan - 31 Jan")
    // XML mein 'tv_selected_date' k liye
    val dateRangeText: String = "All Time History",
    
    )


// ==========================================
// 2️⃣ UI EVENT (User kya action karega)
// ==========================================
sealed class CustomerHistoryUiEvent {

    data class OnDateFilterChanged(val start: Long, val end: Long, val label: String):
        CustomerHistoryUiEvent()
    data class OnSearchQueryChanged(val query: String) : CustomerHistoryUiEvent()
    object OnBackClick : CustomerHistoryUiEvent()
}


// ==========================================
// 3️⃣ UI EFFECT (One-time actions: Navigation/Toast)
// ==========================================
sealed class CustomerHistoryUiEffect {

    object NavigateBack : CustomerHistoryUiEffect()
    // Error ya Success message
    data class ShowSnackbar(val message: String) : CustomerHistoryUiEffect()

}
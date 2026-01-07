package com.miassolutions.milkledger.features.sale.customerhistory

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

    // Header Info (e.g. "All History" or "01 Jan - 31 Jan")
    // XML mein 'tv_selected_date' k liye
    val dateRangeText: String = "All Time History",

    // Bottom Sheet Summary Data
    val summaryMilk: Double = 0.0,      // Total Milk in List
    val summaryReceived: Long = 0,      // Total Payment Received in List
    val currentTotalBalance: Long = 0   // Customer ka Abhi ka Pura Udhaar
)


// ==========================================
// 2️⃣ UI EVENT (User kya action karega)
// ==========================================
sealed class CustomerHistoryUiEvent {
    // Jab user kisi specific transaction (card) par click kare edit k liye
    data class OnTransactionClick(val saleId: String) : CustomerHistoryUiEvent()

    data class OnDateFilterChanged(val start: Long, val end: Long, val label: String):
        CustomerHistoryUiEvent()

    // Agar future mein Date Filter lagana ho (Header click)
    object OnDateFilterClick : CustomerHistoryUiEvent()

    // Back button click
    object OnBackClick : CustomerHistoryUiEvent()
}


// ==========================================
// 3️⃣ UI EFFECT (One-time actions: Navigation/Toast)
// ==========================================
sealed class CustomerHistoryUiEffect {

    // Edit Screen par jane k liye
    data class NavigateToEditSale(val saleId: String) : CustomerHistoryUiEffect()

    // Screen band karne k liye
    object NavigateBack : CustomerHistoryUiEffect()

    // Error ya Success message
    data class ShowSnackbar(val message: String) : CustomerHistoryUiEffect()

    // Date Range Picker kholne k liye (Optional/Future use)
    object ShowDateRangePicker : CustomerHistoryUiEffect()
}
package com.miassolutions.milkledger.features.milk

import com.miassolutions.milkledger.features.sale.domain.model.MilkSaleUiModel


import java.time.LocalDate

// 1️⃣ STATE: Screen par kya dikhana hai
data class MilkSaleListUiState(
    val isLoading: Boolean = false,
    val date: LocalDate = LocalDate.now(),

    // List Data
    val sales: List<MilkSaleUiModel> = emptyList(),

    // Summary Data (Bottom Card k liye)
    val totalMilk: Double = 0.0,
    val totalAmount: Long = 0, // Paisa mein

    val error: String? = null
)

// 2️⃣ EVENT: User kya action le raha hai
sealed interface MilkSaleListUiEvent {
    // --- Date Navigation ---
    data object OnNextDate : MilkSaleListUiEvent
    data object OnPrevDate : MilkSaleListUiEvent
    data object OnDateClick : MilkSaleListUiEvent // Date Picker kholne k liye
    data class OnDateSelected(val date: LocalDate) : MilkSaleListUiEvent

    // --- Actions ---
    data object OnAddSaleClicked : MilkSaleListUiEvent

    // List Item Clicks
    data class OnEditSaleClicked(val saleId: String) : MilkSaleListUiEvent
    data class OnCustomerDetailClicked(val customerId: String) : MilkSaleListUiEvent
}

// 3️⃣ EFFECT: One-time actions (Navigation/Toast)
sealed interface MilkSaleListUiEffect {
    // Naya Sale add karne k liye (Sirf Date pass hogi)

    data object OnDateClick : MilkSaleListUiEffect
    data class NavigateToAddSale(val dateMillis: Long) : MilkSaleListUiEffect


    // Existing Sale edit karne k liye (Transaction ID pass hogi)
    data class NavigateToEditSale(val saleId: String) : MilkSaleListUiEffect

    // Customer ka khata kholne k liye
    data class NavigateToCustomerLedger(val customerId: String) : MilkSaleListUiEffect

    data class ShowSnackbar(val message: String) : MilkSaleListUiEffect
}
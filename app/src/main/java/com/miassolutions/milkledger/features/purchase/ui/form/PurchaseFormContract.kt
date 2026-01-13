package com.miassolutions.milkledger.features.purchase.ui.form


import com.miassolutions.milkledger.features.account.domain.Account
import java.time.LocalDate

// =======================
// 1. STATE
// =======================
data class PurchaseFormUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isEditMode: Boolean = false,

    // Fields
    val selectedSupplier: Account? = null,
    val date: LocalDate = LocalDate.now(),
    val paymentDate: LocalDate = LocalDate.now(),

    val volume: String = "",
    val fat: String = "",
    val lr: String = "",
    val rate: String = "",

    // Payment
    val amountPaid: String = "",
    val note: String = "",

    // Calculated / Display Fields
    val calculatedTs: Double = 0.0,    // UI par dikhane k liye
    val calculatedTotal: Long = 0, // Total Bill
    val currentBalance: Long = 0
)

// =======================
// 2. EVENTS
// =======================
sealed class PurchaseFormUiEvent {
    // Inputs
    data class OnSupplierSelected(val supplier: Account) : PurchaseFormUiEvent()
    data class OnVolumeChanged(val value: String) : PurchaseFormUiEvent()
    data class OnFatChanged(val value: String) : PurchaseFormUiEvent()
    data class OnLrChanged(val value: String) : PurchaseFormUiEvent()
    data class OnRateChanged(val value: String) : PurchaseFormUiEvent()
    data class OnAmountPaidChanged(val value: String) : PurchaseFormUiEvent()
    data class OnNoteChanged(val value: String) : PurchaseFormUiEvent()

    // Date Pickers
    data class OnDateSelected(val date: LocalDate) : PurchaseFormUiEvent()
    data class OnPaymentDateSelected(val date: LocalDate) : PurchaseFormUiEvent()
    object OnDateClick : PurchaseFormUiEvent()
    object OnPaymentDateClick : PurchaseFormUiEvent()

    // Actions
    object OnSaveClicked : PurchaseFormUiEvent()
    object OnSaveAndNewClicked : PurchaseFormUiEvent()

    data object OnDeleteClicked : PurchaseFormUiEvent()
    object OnBackClicked : PurchaseFormUiEvent() // Optional
}

// =======================
// 3. EFFECTS
// =======================
sealed class PurchaseFormUiEffect {
    data class ShowSnackbar(val message: String) : PurchaseFormUiEffect()
    object NavigateBack : PurchaseFormUiEffect()
    object OpenDatePicker : PurchaseFormUiEffect()

    object OpenPaymentDatePicker : PurchaseFormUiEffect()
}
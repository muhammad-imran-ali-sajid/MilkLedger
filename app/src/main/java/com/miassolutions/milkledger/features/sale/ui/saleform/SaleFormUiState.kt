package com.miassolutions.milkledger.features.sale.ui.saleform

import com.miassolutions.milkledger.features.account.domain.Account
import com.miassolutions.milkledger.utils.extensions.toMilkAmount
import com.miassolutions.milkledger.utils.extensions.toPrice
import java.time.LocalDate


/* New Ledger type Sale*/

data class SaleFormUiState(
    val isEditMode: Boolean = false,

    val isLoading: Boolean = false,

    val date: LocalDate = LocalDate.now(),
    val selectedCustomer: Account? = null,
    val currentBalance: Long = 0, // Paisa

    val volume: String = "",
    val deduction: String = "",
    val rate: String = "",

    val paymentDate: LocalDate = LocalDate.now(),
    val amountPaid: String = "", // Payment (Optional)
    val note: String = "",

    val calculatedTotal: Double = 0.0, // Live Calculation
    val isSaving: Boolean = false
) {


    private val volumeDouble: Double
        get() = volume.toDoubleOrNull() ?: 0.0

    private val deductionDouble: Double
        get() = deduction.toDoubleOrNull() ?: 0.0

    private val rateDouble: Double
        get() = rate.toDoubleOrNull() ?: 0.0

    private val netMilk: Double
        get() = volumeDouble - deductionDouble

    val displayRate: String
        get() = "Rate: $rateDouble"
    val displayNetMilk: String
        get() = "Net Milk: ${netMilk.toMilkAmount()}"


}

sealed interface SaleFormUiEvent {
    data object OnDateClick : SaleFormUiEvent

    data object OnPaymentDateClick : SaleFormUiEvent

    data class LoadSaleForEdit(val saleId: String) : SaleFormUiEvent

    data class OnPaymentDateSelected(val paymentDate: LocalDate) : SaleFormUiEvent

    data class OnDateSelected(val date: LocalDate) : SaleFormUiEvent
    data class OnCustomerSelected(val customer: Account) : SaleFormUiEvent

    data class OnVolumeChanged(val value: String) : SaleFormUiEvent
    data class OnDeductionChanged(val value: String) : SaleFormUiEvent
    data class OnRateChanged(val value: String) : SaleFormUiEvent
    data class OnAmountPaidChanged(val value: String) : SaleFormUiEvent
    data class OnNoteChanged(val value: String) : SaleFormUiEvent

    data object OnSaveClicked : SaleFormUiEvent
}

sealed interface SaleFormUiEffect {

    data object OpenPaymentDatePicker : SaleFormUiEffect
    data object OpenDatePicker : SaleFormUiEffect
    data object NavigateBack : SaleFormUiEffect
    data class ShowSnackbar(val message: String) : SaleFormUiEffect
}


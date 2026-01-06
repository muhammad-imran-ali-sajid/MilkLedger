package com.miassolutions.milkledger.features.sale.ui.saleform

import com.miassolutions.milkledger.features.account.domain.Account
import com.miassolutions.milkledger.features.customer.ui.model.DropDownCustomerListUi
import com.miassolutions.milkledger.utils.extensions.toMilkAmount
import com.miassolutions.milkledger.utils.extensions.toPrice
import java.time.LocalDate

//data class SaleFormUiState(
//    val mode: SaleMode = SaleMode.ADD,
//    val saleId: String? = null,
//
//    val saleDate: LocalDate = LocalDate.now(),
//    val customers: List<DropDownCustomerListUi> = emptyList(),
//
//    val selectedCustomer: DropDownCustomerListUi? = null,
//
//    val volume: String = "",
//    val deduction: String = "",
//
//    val netMilk: Double = 0.0,
//
//    val rateUsed: Double = 0.0,
//    val price: Double = 0.0,
//
//    val receivedAmount: String = "",
//    val receivedDate: LocalDate = LocalDate.now(),
//
//    val balance: Double = 0.0,
//
//    val notes: String = "",
//
//    val isSaving: Boolean = false,
//    val error: String? = null
//
//)
//
//sealed interface SaleFormUiEvent {
//
//    data class EditSaleLoaded(val saleId: String) : SaleFormUiEvent
//    data class CustomerSelected(
//        val customerId: String,
//        val customerName: String,
//        val rate: Double,
//    ) : SaleFormUiEvent
//
//    data class VolumeChanged(val value: String) : SaleFormUiEvent
//    data class DeductionChanged(val value: String) : SaleFormUiEvent
//    data class PaymentChanged(val value: String) : SaleFormUiEvent
//    data class NotesChanged(val value: String) : SaleFormUiEvent
//
//    data object SaleDateClicked : SaleFormUiEvent
//
//    data class SaleDateSelected(val date: LocalDate) : SaleFormUiEvent
//    data class ReceivedDateSelected(val date: LocalDate) : SaleFormUiEvent
//    data object ReceivedDateClicked : SaleFormUiEvent
//
//    data object SaveClicked : SaleFormUiEvent
//    data object SaveAndNewClicked : SaleFormUiEvent
//}
//
//
//sealed interface SaleFormUiEffect {
//
//    data class ShowToast(val message: String) : SaleFormUiEffect
//
//    data object OpenSaleDatePicker : SaleFormUiEffect
//    data object OpenReceivedDatePicker : SaleFormUiEffect
//
//    data object NavigateBack : SaleFormUiEffect
//    data object ResetForm : SaleFormUiEffect
//}
//
//enum class SaleMode {
//    ADD,
//    EDIT
//}

/* New Ledger type Sale*/

data class SaleFormUiState(
    val date: LocalDate = LocalDate.now(),
    val selectedCustomer: Account? = null,
    val currentBalance: Long = 0, // Paisa

    val volume: String = "",
    val deduction: String = "",
    val rate: String = "",

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
    data object OpenDatePicker : SaleFormUiEffect
    data object NavigateBack : SaleFormUiEffect
    data class ShowSnackbar(val message: String) : SaleFormUiEffect
}


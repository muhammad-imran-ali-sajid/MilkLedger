package com.miassolutions.milkledger.features.sale.ui.saleform

import com.miassolutions.milkledger.features.customer.ui.model.DropDownCustomerListUi
import java.time.LocalDate

data class SaleFormUiState(
    val mode: SaleMode = SaleMode.ADD,
    val saleDate: LocalDate = LocalDate.now(),

    val customers: List<DropDownCustomerListUi> = emptyList(),
    val selectedCustomer: DropDownCustomerListUi? = null,

    val volume: String = "",
    val deduction: String = "",

    val netMilk: Double = 0.0,

    val rateUsed: Double = 0.0,
    val price: Double = 0.0,

    val receivedAmount: String = "",
    val receivedDate: LocalDate = LocalDate.now(),

    val balance: Double = 0.0,

    val notes: String = "",

    val isSaving: Boolean = false,
    val error: String? = null

)

sealed interface SaleFormUiEvent {

    data class CustomerSelected(
        val customerId: String,
        val customerName: String,
        val rate: Double,
    ) : SaleFormUiEvent

    data class VolumeChanged(val value: String) : SaleFormUiEvent
    data class DeductionChanged(val value: String) : SaleFormUiEvent
    data class PaymentChanged(val value: String) : SaleFormUiEvent
    data class NotesChanged(val value: String) : SaleFormUiEvent

    data object SaleDateClicked : SaleFormUiEvent

    data class SaleDateSelected(val date: LocalDate) : SaleFormUiEvent
    data class ReceivedDateSelected(val date: LocalDate) : SaleFormUiEvent
    data object ReceivedDateClicked : SaleFormUiEvent

    data object SaveClicked : SaleFormUiEvent
    data object SaveAndNewClicked : SaleFormUiEvent
}


sealed interface SaleFormUiEffect {

    data class ShowToast(val message: String) : SaleFormUiEffect

    data object OpenSaleDatePicker: SaleFormUiEffect
    data object OpenReceivedDatePicker: SaleFormUiEffect

    data object NavigateBack: SaleFormUiEffect
    data object ResetForm: SaleFormUiEffect
}

enum class SaleMode {
    ADD,
    EDIT
}
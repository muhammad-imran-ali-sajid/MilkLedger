package com.miassolutions.milkledger.presentation.customerandsales.sales.saleform.state

import java.time.LocalDate

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
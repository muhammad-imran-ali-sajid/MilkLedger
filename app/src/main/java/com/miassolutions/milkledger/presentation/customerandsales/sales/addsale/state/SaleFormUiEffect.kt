package com.miassolutions.milkledger.presentation.customerandsales.sales.addsale.state

sealed interface SaleFormUiEffect {

    data class ShowToast(val message: String) : SaleFormUiEffect

    data object OpenSaleDatePicker: SaleFormUiEffect
    data object OpenReceivedDatePicker: SaleFormUiEffect

    data object NavigateBack: SaleFormUiEffect
    data object ResetForm: SaleFormUiEffect
}
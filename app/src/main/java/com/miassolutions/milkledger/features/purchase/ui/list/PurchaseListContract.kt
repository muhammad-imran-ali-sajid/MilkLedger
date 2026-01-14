package com.miassolutions.milkledger.features.purchase.ui.list

import com.miassolutions.milkledger.features.purchase.model.MilkPurchaseUiModel
import com.miassolutions.milkledger.features.purchase.model.PurchaseSummary
import java.time.LocalDate

data class PurchaseListUiState(
    val isLoading: Boolean = false,
    val date: LocalDate = LocalDate.now(),
    val purchases: List<MilkPurchaseUiModel> = emptyList(),

    val summary: PurchaseSummary = PurchaseSummary(),

    // Summary
    val totalVolume: Double = 0.0,
    val totalPrice: Long = 0,
    val totalPaid: Long = 0
)

sealed class PurchaseListUiEvent {
    data class OnDateSelected(val date: LocalDate) : PurchaseListUiEvent()
    object OnDateClick : PurchaseListUiEvent()
    object OnAddPurchaseClick : PurchaseListUiEvent()

    data class OnSupplierHistoryClick(val supplierId: String, val supplierName: String) :
        PurchaseListUiEvent()


    data class OnEditClick(val purchaseId: String) : PurchaseListUiEvent()
    data class OnBalanceClick(val supplierId: String, val supplierName: String) :
        PurchaseListUiEvent()

    data class OnDeleteClick(val purchaseId: String) : PurchaseListUiEvent()
}

sealed class PurchaseListUiEffect {
    data class ShowSnackbar(val message: String) : PurchaseListUiEffect()
    object OpenDatePicker : PurchaseListUiEffect()

    data class OpenSupplierHistory(val supplierId: String, val supplierName: String) :
        PurchaseListUiEffect()

    object NavigateToAddPurchase : PurchaseListUiEffect()
    data class NavigateToEditPurchase(val id: String) : PurchaseListUiEffect()
    data class OpenBalanceHistorySheet(val id: String, val name: String) : PurchaseListUiEffect()
}
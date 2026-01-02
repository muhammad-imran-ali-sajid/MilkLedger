package com.miassolutions.milkledger.features.sale.ui.list

import com.miassolutions.milkledger.features.sale.domain.model.BalanceHistoryItem
import com.miassolutions.milkledger.features.sale.domain.model.SaleUi
import java.time.LocalDate

//data class SalesUiState(
//    val currentDate: LocalDate = LocalDate.now(),
//    val salesForDate: List<Sale> = emptyList(),
//    val salesUi: List<SaleUi> = emptyList(),
//    val grandSaleTotalForDate: Double = 0.0,
//    val selectedCustomerId: String? = null,
//    val navToLedgerForCustomerId: String? = null,
//    val pdfSalesSummary: PdfSalesSummary = PdfSalesSummary(),
//    val pfdSalesItemRecord: PdfSalesItemRecord = PdfSalesItemRecord(),
//
//    val totalMilk: Double = 0.0,
//    val totalNetMilk: Double = 0.0,
//    val totalDeduction: Double = 0.0,
//    val totalPaid: Double = 0.0,
//    val totalBalance: Double = 0.0,
//    val receivedAmount: Double = 0.0,
//    val avgRatePerLiter: Double = 0.0,
//
//    val isLoading  : Boolean = false
//)

data class SalesUiState(
    val isLoading: Boolean = false,
    val currentDate: LocalDate = LocalDate.now(),

    val sales: List<SaleUi> = emptyList(),



    val totalMilk: Double = 0.0,
    val totalDeduction: Double = 0.0,
    val totalNetMilk: Double = 0.0,

    val totalAmount: Double = 0.0,
    val receivedAmount: Double = 0.0,
    val totalBalance: Double = 0.0,

    val avgRatePerLiter: Double = 0.0
)


sealed interface SalesUiEvent {
    data class EditClicked(val sale: SaleUi) : SalesUiEvent
    data class SelectDate(val date: LocalDate) : SalesUiEvent
    data object NextDate : SalesUiEvent
    data object PreviousDate : SalesUiEvent

    data class DeleteSale(val saleId: String) : SalesUiEvent

    data class EditSale(
        val saleId: String,
        val volume: Double,
        val deduction: Double,
        val rate: Double,
        val paid: Double,
        val paidAt: LocalDate?,
        val notes: String?
    ) : SalesUiEvent


    data class BalanceClicked(
        val customerId: String,
        val customerName: String
    ) : SalesUiEvent

    object DismissBalanceHistory : SalesUiEvent


    data class OpenCustomerLedger(
        val customerId: String,
        val customerName: String
    ) : SalesUiEvent


}

sealed interface SalesUiEffect {
    data class NavigateToCustomerLedger(
        val customerId: String,
        val name: String
    ) : SalesUiEffect

    data class EditSaleRecord(val saleUi: SaleUi) : SalesUiEffect

    data class ShowBalanceHistory(
        val customerName: String,
        val historyItem: List<BalanceHistoryItem>
    ) :
        SalesUiEffect

    data class ShowMessage(val message: String) : SalesUiEffect

//    data class GeneratePdf(val data: SalesPdfData) : SalesUiEffect
}
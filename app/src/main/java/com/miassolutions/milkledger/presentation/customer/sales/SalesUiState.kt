package com.miassolutions.milkledger.presentation.customer.sales

import com.miassolutions.milkledger.core.pdf.salereport.PdfSalesItemRecord
import com.miassolutions.milkledger.core.pdf.salereport.PdfSalesSummary
import com.miassolutions.milkledger.data.local.relations.SaleWithCustomer
import com.miassolutions.milkledger.domain.model.Sale
import java.time.LocalDate

data class SalesUiState(
    val currentDate: LocalDate = LocalDate.now(),
    val salesForDate: List<Sale> = emptyList(),
    val salesUi: List<SaleUi> = emptyList(),
    val grandSaleTotalForDate: Double = 0.0,
    val selectedCustomerId: String? = null,
    val navToLedgerForCustomerId: String? = null,
    val pdfSalesSummary: PdfSalesSummary = PdfSalesSummary(),
    val pfdSalesItemRecord: PdfSalesItemRecord = PdfSalesItemRecord(),
//    val salePdfReport: SalePdfReport = SalePdfReport(),
    val totalMilk: Double = 0.0,
    val totalNetMilk: Double = 0.0,
    val totalDeduction: Double = 0.0,
    val totalPaid: Double = 0.0,
    val totalBalance: Double = 0.0,
    val receivedAmount: Double = 0.0,
    val avgRatePerLiter: Double = 0.0,

    val isLoading  : Boolean = false
)




sealed class SalesUiEvent {
    data class OnCustomerSelected(val supplierId: String) : SalesUiEvent()
    data class SelectDate(val date: LocalDate) : SalesUiEvent()
}
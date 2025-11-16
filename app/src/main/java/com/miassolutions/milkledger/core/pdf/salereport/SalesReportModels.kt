package com.miassolutions.milkledger.core.pdf.salereport


data class SalesReportPdf(
    val date: String,
    val recordList: List<PdfSalesItemRecord>,
    val salesSummary: PdfSalesSummary,
    val footerNote: String? = null
)

data class PdfSalesSummary(
    val totalQty: String = "",
    val totalDeduction: String = "",
    val totalAmount: String = "",
    val totalPaid: String = "",
    val balanceDue: String = ""
)

data class PdfSalesItemRecord(
    val customerName: String = "",
    val milkVolume: String = "",
    val deduction: String = "",
    val rate: String = "",
    val amount: String = "",
    val paid: String = "",
    val balance: String = "",
)
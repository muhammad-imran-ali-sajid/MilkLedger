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
    val milkVolume: Double = 0.0,
    val deduction: Double = 0.0,
    val rate: Double = 0.0,
    val amount: Double = 0.0,
    val paid: Double= 0.0,
    val balance: Double= 0.0,
)
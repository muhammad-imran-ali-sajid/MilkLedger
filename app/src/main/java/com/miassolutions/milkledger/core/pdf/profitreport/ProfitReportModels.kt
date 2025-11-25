package com.miassolutions.milkledger.core.pdf.profitreport

data class ProfitReceiptPdf(
    val dateRange: String,
    val profitReceiver: String,
    val recordList: List<PdfProfitItemRecord>,
    val totalProfit:String,
    val totalReceived : String,
    val totalBalance: String,
    val footerNote: String? = null
)

data class PdfProfitItemRecord(
    val date: String,
    val profit: Double,
    val profitReceived: Double,
    val balance: Double
)
package com.miassolutions.milkledger.core.pdf.profitreport

data class ProfitReceiptPdf(
    val dateRange: String,
    val profitReceiver: String,
    val recordList: List<PdfProfitItemRecord>,
    val accGrossProfit:String,
    val accNetProfit: String,
    val accReceived : String,
    val accBalance: String,
    val footerNote: String? = null
)

data class PdfProfitItemRecord(
    val date: String,
    val grossProfit: Double,
    val netProfit: Double,
    val profitReceived: Double,
    val balance: Double
)

data class PdfProfitSummary(
    val totalProfit : Double,
    val totalReceived: Double,
    val balance : Double
)
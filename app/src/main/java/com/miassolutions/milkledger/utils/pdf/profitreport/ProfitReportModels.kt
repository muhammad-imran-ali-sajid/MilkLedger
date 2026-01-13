package com.miassolutions.milkledger.utils.pdf.profitreport

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
    val grossProfit: Long,
    val netProfit: Long,
    val profitReceived: Long,
    val balance: Long
)

data class PdfProfitSummary(
    val totalProfit : Long,
    val totalReceived: Long,
    val balance : Long
)
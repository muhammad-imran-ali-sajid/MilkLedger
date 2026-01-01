package com.miassolutions.milkledger.utils.pdf.purchasereport

data class PurchaseReportPdf(
    val date: String,
    val recordList: List<PdfPurchaseItemRecord>,
    val pdfPurchaseSummary: PdfPurchaseSummary,
    val footerNote: String? = null
)

data class PdfPurchaseSummary(
    val totalQty: String,
    val avgFat : String,
    val avgLr : String,
    val totalTs : String,
    val avgRate : String,
    val totalAmount: String,
    val totalPaid: String,
    val balanceDue: String
)

data class PdfPurchaseItemRecord(
    val supplierName: String,
    val milkVolume: String,
    val fat: String,
    val lr: String,
    val ts: String,
    val rate : String,
    val amount: String,
    val paid: String,
    val balance: String,
)
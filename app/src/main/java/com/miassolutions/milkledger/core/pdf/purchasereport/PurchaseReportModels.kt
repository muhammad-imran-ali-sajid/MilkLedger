package com.miassolutions.milkledger.core.pdf.purchasereport

data class PurchaseReceiptPdf(
    val date: String,
    val recordList: List<PurchaseItemRecord>,
    val purchaseSummary: PurchaseSummary,
    val footerNote: String? = null
)

data class PurchaseSummary(
    val totalQty: String,
    val avgFat : String,
    val avgLr : String,
    val totalTs : String,
    val totalAmount: String,
    val totalPaid: String,
    val balanceDue: String
)

data class PurchaseItemRecord(
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
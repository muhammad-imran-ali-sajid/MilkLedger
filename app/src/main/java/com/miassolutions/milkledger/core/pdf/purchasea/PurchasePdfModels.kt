package com.miassolutions.milkledger.core.pdf.purchasea

data class PurchaseReceiptPdf(
    val dateRange: String,
    val partyName: String,
    val recordList: List<PurchaseItemRecord>,
    val totalAmount: String,
    val totalPaid : String,
    val totalBalance : String,
    val footerNote: String? = null
)

data class PurchaseItemRecord(
    val date: String,
    val quantity: Double,
    val ts: Double,
    val rate: Double,
    val amount: Double,
    val paid: Double,
    val balance: Double
)
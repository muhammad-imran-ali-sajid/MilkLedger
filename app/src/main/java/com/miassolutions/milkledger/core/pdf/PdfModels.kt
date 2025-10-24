package com.miassolutions.milkledger.core.pdf

data class PdfReceiptData(
    val title: String,
    val dateRange: String,
    val partyName: String,
    val recordList: List<RecordItem>,
    val totalAmount: String,
    val totalPaid : String,
    val totalBalance : String,
    val footerNote: String? = null
)

data class RecordItem(
    val date: String,
    val quantity: Double,
    val ts: Double,
    val rate: Double,
    val amount: Double,
    val paid: Double,
    val balance: Double
)
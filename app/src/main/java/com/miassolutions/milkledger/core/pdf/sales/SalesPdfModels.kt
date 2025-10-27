package com.miassolutions.milkledger.core.pdf.sales

data class SalesReceiptPdf(
    val dateRange: String,
    val partyName: String,
    val recordList: List<SalesItemRecord>,
    val totalAmount: String,
    val totalPaid: String,
    val totalBalance: String,
    val footerNote: String? = null
)

data class SalesItemRecord(
    val date: String,
    val quantity: Double,
    val deduction: Double,
    val rate: Double,
    val amount: Double,
    val paid: Double,
    val balance: Double
)



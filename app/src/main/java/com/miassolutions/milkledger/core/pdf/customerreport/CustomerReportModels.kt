package com.miassolutions.milkledger.core.pdf.customerreport

data class SalesReceiptPdf(
    val dateRange: String,
    val partyName: String,
    val recordList: List<SalesItemRecord>,
    val totalVolume : String,
    val totalDeduction : String,
    val totalNetMilk : String,
    val totalAmount: String,
    val totalPaid: String,
    val totalBalance: String,
    val footerNote: String? = null
)

data class SalesItemRecord(
    val date: String,
    val quantity: Double,
    val deduction: Double,
    val netMilk : Double,
    val rate: Double,
    val amount: Double,
    val paid: Double,
    val balance: Double
)



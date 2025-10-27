package com.miassolutions.milkledger.core.pdf.supplierreport

data class SupplierReceiptPdf(
    val dateRange: String,
    val partyName: String,
    val recordList: List<SupplierItemRecord>,
    val totalAmount: String,
    val totalPaid : String,
    val totalBalance : String,
    val footerNote: String? = null
)

data class SupplierItemRecord(
    val date: String,
    val quantity: Double,
    val ts: Double,
    val rate: Double,
    val amount: Double,
    val paid: Double,
    val balance: Double
)
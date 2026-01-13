package com.miassolutions.milkledger.utils.pdf.supplierreport

data class SupplierReceiptPdf(
    val dateRange: String,
    val supplierName: String,
    val recordList: List<PdfSupplierItemRecord>,
    val totalQty:String,
    val avgTs : String,
    val totalAmount: String,
    val totalPaid : String,
    val totalBalance : String,
    val footerNote: String? = null
)

data class PdfSupplierItemRecord(
    val date: String,
    val quantity: Double,
    val ts: Double,
    val rate: Long,
    val amount: Long,
    val paid: Long,
    val balance: Long
)
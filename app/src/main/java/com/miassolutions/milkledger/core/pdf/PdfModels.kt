package com.miassolutions.milkledger.core.pdf

import java.time.LocalDate

data class PdfReceiptData(
    val title: String,
    val date: LocalDate,
    val partyName: String,
    val recordList: List<RecordItem>,
    val totalAmount: Double,
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
package com.miassolutions.milkledger.features.customer.domain.model


import java.time.LocalDate

data class CustomerHistoryUi(
    val date: LocalDate,
    val description: String, // e.g. "Milk Sale (40L)" or "Cash Received"
    val amount: Long,        // Paisa
    val type: HistoryType    // DEBIT (Red) or CREDIT (Green)
)

enum class HistoryType { DEBIT, CREDIT }
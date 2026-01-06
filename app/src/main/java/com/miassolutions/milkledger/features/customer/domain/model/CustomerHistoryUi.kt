package com.miassolutions.milkledger.features.customer.domain.model


import java.time.LocalDate

data class CustomerHistoryUi(
    val date: LocalDate,
    val description: String, // e.g. "Milk Sale (40L)" or "Cash Received"
    val amount: Long,        // Paisa
    val type: HistoryType    // DEBIT (Red) or CREDIT (Green)
)

enum class HistoryType { DEBIT, CREDIT }


data class BalanceHistoryUi(
    val id: String,
    val date: LocalDate,
    val description: String,

    // Transaction Amount
    val amount: Long,      // Paisa (Jo abhi hua)
    val isDebit: Boolean,  // True = Sale (Red), False = Payment (Green)

    // 🔥 The Main Thing: Running Balance
    val runningBalance: Long
)
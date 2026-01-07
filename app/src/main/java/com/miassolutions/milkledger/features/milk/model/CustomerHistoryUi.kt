package com.miassolutions.milkledger.features.milk.model


import java.time.LocalDate

data class CustomerHistoryUi(
    val date: LocalDate,
    val description: String, // e.g. "Milk Sale (40L)" or "Cash Received"
    val amount: Long,        // Paisa
    val type: HistoryType    // DEBIT (Red) or CREDIT (Green)
)

enum class HistoryType { DEBIT, CREDIT }


data class BalanceHistoryUi(
    val date: LocalDate,
    val closingBalance: Long, // Us din k ikhtitaam pr Running Balance
    val netChange: Long       // Us din total kitna plus/minus hua (Optional display k liye)
)


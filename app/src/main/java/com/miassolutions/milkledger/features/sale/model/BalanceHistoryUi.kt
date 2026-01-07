package com.miassolutions.milkledger.features.sale.model


import java.time.LocalDate
data class BalanceHistoryUi(
    val date: LocalDate,
    val closingBalance: Long, // Us din k ikhtitaam pr Running Balance
    val netChange: Long       // Us din total kitna plus/minus hua (Optional display k liye)
)


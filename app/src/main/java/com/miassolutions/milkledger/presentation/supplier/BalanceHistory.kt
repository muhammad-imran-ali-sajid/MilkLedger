package com.miassolutions.milkledger.presentation.supplier

import java.time.LocalDate

data class BalanceHistory(
    val date : LocalDate,
    val balance : Double
)

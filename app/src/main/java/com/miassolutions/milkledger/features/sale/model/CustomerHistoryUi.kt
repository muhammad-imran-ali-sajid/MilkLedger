package com.miassolutions.milkledger.features.sale.model

import java.time.LocalDate

data class CustomerHistoryUi(
    val date: LocalDate,
    val description: String,
    val amount: Long,        // Paisa
)
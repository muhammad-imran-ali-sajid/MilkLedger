package com.miassolutions.milkledger.domain.model

import java.time.LocalDate

data class ProfitReceipt(
    val id: String,
    val date: LocalDate,
    val receivedFromId: String,
    val amount: Double,
    val note: String?
)


package com.miassolutions.milkledger.features.purchase.domain

import java.time.LocalDate

data class Purchase(
    val id: String,
    val supplierId: String,
    val date: LocalDate,
    val milkAmount: Double,
    val fat: Double,
    val lr: Double,
    val ts: Double,
    val milkPrice: Double,
    val payment: Double,
    val balance: Double,
    val rateUsed: Double,
    val notes: String?
)
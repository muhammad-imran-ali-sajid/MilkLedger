package com.miassolutions.milkledger.domain.model

import java.time.LocalDate

data class Sale(
    val customerId : String,
    val saleId : String,
    val saleDate : LocalDate,
    val name: String,
    val rate : Double,
    val volume: Double,
    val deduction: Double,
    val netVolume: Double,
    val price: Double,
    val received: Double,
    val receivedDate: LocalDate?,
    val balance: Double,
    val notes : String?
)

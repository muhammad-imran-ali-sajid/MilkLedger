package com.miassolutions.milkledger.domain.model

import java.time.LocalDate

data class SaleWithCustomerModel(
    val id: String,
    val customerId: String,
    val name : String,
    val date: LocalDate,
    val paidAt: LocalDate?,
    val volume: Double,
    val deduction: Double,
    val netMilk: Double,
    val price: Double,
    val paid: Double,
    val balance: Double,
    val rateUsed: Double,
    val notes: String?
)

data class Sale(
    val id: String,
    val customerId: String,
    val date: LocalDate,
    val paidAt: LocalDate?,
    val volume: Double,
    val deduction: Double,
    val netMilk: Double,
    val price: Double,
    val paid: Double,
    val balance: Double,
    val rateUsed: Double,
    val notes: String?
)


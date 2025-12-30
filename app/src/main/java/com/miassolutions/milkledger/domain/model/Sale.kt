package com.miassolutions.milkledger.domain.model

import java.time.LocalDate

//data class SaleWithCustomerModel(
//    val id: String,
//    val customerId: String,
//    val name : String,
//    val date: LocalDate,
//    val paidAt: LocalDate?,
//    val volume: Double,
//    val deduction: Double,
//    val netMilk: Double,
//    val price: Double,
//    val paid: Double,
//    val balance: Double,
//    val rateUsed: Double,
//    val notes: String?
//)



data class SaleUi(
    val id: String,
    val customerId: String,
    val customerName: String,
    val date: LocalDate,
    val volume: Double,
    val deduction: Double,
    val netMilk: Double,
    val price: Double,
    val paid: Double,
    val balance: Double,
    val rateUsed: Double,
    val notes: String?,
    val accumulatedBalance: Double
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


data class SaleProjection(
    val sale: Sale,
    val customerName: String,
    val accumulatedBalance: Double
)

fun Sale.toSaleUi(
    customerName: String,
    accumulatedBalance: Double
): SaleUi =
    SaleUi(
        id = id,
        customerId = customerId,
        customerName = customerName,
        date = date,
        volume = volume,
        deduction = deduction,
        netMilk = netMilk,
        price = price,
        paid = paid,
        balance = balance,
        rateUsed = rateUsed,
        notes = notes,
        accumulatedBalance = accumulatedBalance
    )




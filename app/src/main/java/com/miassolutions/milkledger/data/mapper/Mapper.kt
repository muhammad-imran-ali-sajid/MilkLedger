package com.miassolutions.milkledger.data.mapper


import com.miassolutions.milkledger.presentation.customer.sales.db.SalesEntity
import com.miassolutions.milkledger.domain.model.Sale
import java.time.LocalDate

/**
 * Maps a Sale domain model to a SalesEntity for Room persistence
 */
fun Sale.toEntity(
    saleId: String,
    saleDate: LocalDate,
    rateUsed: Double
): SalesEntity {
    return SalesEntity(
        saleId = saleId,
        customerId = customerId,
        date = saleDate,
        volume = volume,
        deduction = deduction,
        netMilk = netVolume,
        price = price,
        paid = received,
        balance = balance,
        rateUsed = rateUsed,
        notes = notes,
        paidDate = receivedDate
    )
}

fun SalesEntity.toSale(customerName: String): Sale {
    return Sale(
        customerId = customerId,
        name = customerName,
        rate = rateUsed,
        volume = volume,
        deduction = deduction,
        netVolume = netMilk,
        price = price,
        received = paid,
        receivedDate = paidDate,
        balance = balance,
        notes = notes,
        saleId = saleId,
        saleDate = date
    )
}


package com.miassolutions.milkledger.data.mapper

import com.miassolutions.milkledger.core.util.toDisplayDate
import com.miassolutions.milkledger.core.util.toPriceStr
import com.miassolutions.milkledger.core.util.toRoundedStr
import com.miassolutions.milkledger.data.local.entities.SalesEntity
import com.miassolutions.milkledger.data.local.relations.SaleWithCustomer
import com.miassolutions.milkledger.data.remote.model.FirestoreSales
import com.miassolutions.milkledger.domain.model.Sale
import java.time.LocalDate
import java.time.format.DateTimeFormatter

fun SalesEntity.toFirestoreModel(): FirestoreSales {
    return FirestoreSales(
        saleId = saleId,
        customerId = customerId,
        date = date.format(DateTimeFormatter.ISO_LOCAL_DATE),
        volume = volume,
        deduction = deduction,
        netMilk = netMilk,
        price = price,
        paid = paid,
        balance = balance,
        rateUsed = rateUsed,
        notes = notes,

        paidDate = paidDate?.format(DateTimeFormatter.ISO_LOCAL_DATE),

        isSynced = isSynced,
        updatedAt = updatedAt,
        deletedAt = deletedAt
    )
}

fun SaleWithCustomer.toSalesList(): Sale {
    return Sale(
        customerId = customer.customerId,
        saleId = sale.saleId,
        saleDate = sale.date,
        name = customer.customerName,
        rate = sale.rateUsed,
        volume = sale.volume,
        deduction = sale.deduction,
        netVolume = sale.netMilk,
        price = sale.price,
        received = sale.paid,
        receivedDate = sale.paidDate,
        balance = sale.balance,
        notes = sale.notes
    )
}

fun Sale.toSalesEntity(existingSaleId: String, rateUsed: Double, date: LocalDate): SalesEntity {
    return SalesEntity(
        saleId = existingSaleId,
        customerId = this.customerId,
        date = date,
        volume = this.volume,
        deduction = this.deduction,
        netMilk = this.netVolume,
        price = this.price,
        paid = this.received,
        balance = this.balance,
        rateUsed = rateUsed,
        notes = this.notes
    )
}




fun FirestoreSales.toEntityModel(): SalesEntity {
    return SalesEntity(
        saleId = saleId,
        customerId = customerId,
        date = LocalDate.parse(date),
        volume = volume,
        deduction = deduction,
        netMilk = netMilk,
        price = price,
        paid = paid,
        balance = balance,
        rateUsed = rateUsed,
        paidDate = LocalDate.parse(paidDate),
        notes = notes,
        isSynced = isSynced,
        updatedAt = updatedAt,
        deletedAt = deletedAt
    )
}
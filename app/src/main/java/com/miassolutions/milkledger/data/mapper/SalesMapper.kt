package com.miassolutions.milkledger.data.mapper

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
        isSynced = isSynced,
        updatedAt = updatedAt,
        deletedAt = deletedAt
    )
}

fun SaleWithCustomer.toSalesList() : Sale{
    return Sale(
        name = this.customer.customerName,
        volume = this.sale.volume.toRoundedStr(),
        deduction = this.sale.deduction.toRoundedStr(),
        netVolume = this.sale.netMilk.toRoundedStr(),
        price = this.sale.price.toPriceStr(),
        received = this.sale.paid.toRoundedStr(),
        receivedDate = this.sale.updatedAt,
        balance = this.sale.balance.toPriceStr()
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
        notes = notes,
        isSynced = isSynced,
        updatedAt = updatedAt,
        deletedAt = deletedAt
    )
}
package com.miassolutions.milkledger.data.mapper

import com.miassolutions.milkledger.data.local.entities.SalesEntity
import com.miassolutions.milkledger.data.remote.model.FirestoreSales
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
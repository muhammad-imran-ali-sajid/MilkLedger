package com.miassolutions.milkledger.data.mapper

import com.miassolutions.milkledger.utils.extensions.toLocalDate
import com.miassolutions.milkledger.utils.extensions.toMillis
import com.miassolutions.milkledger.data.local.entities.SalesEntity
import com.miassolutions.milkledger.domain.model.Sale

fun SalesEntity.toDomain(): Sale =
    Sale(
        id = saleId,
        customerId = customerId,
        date = dateMillis.toLocalDate(),
        paidAt = paidAtMillis?.toLocalDate(),
        volume = volume,
        deduction = deduction,
        netMilk = netMilk,
        price = totalAmount,
        paid = paid,
        balance = balance,
        rateUsed = rateUsed,
        notes = notes
    )

fun Sale.toEntity(): SalesEntity =
    SalesEntity(
        saleId = id,
        customerId = customerId,
        dateMillis = date.toMillis(),
        paidAtMillis = paidAt?.toMillis(),
        volume = volume,
        deduction = deduction,
        netMilk = netMilk,
        totalAmount = price,
        paid = paid,
        balance = balance,
        rateUsed = rateUsed,
        notes = notes
    )

package com.miassolutions.milkledger.data.mapper

import com.miassolutions.milkledger.utils.extensions.toLocalDate
import com.miassolutions.milkledger.utils.extensions.toMillis
import com.miassolutions.milkledger.data.local.entities.PurchaseEntity
import com.miassolutions.milkledger.domain.model.Purchase

fun PurchaseEntity.toDomain(): Purchase =
    Purchase(
        id = purchaseId,
        supplierId = supplierId,
        date = dateMillis.toLocalDate(),
        milkAmount = milkAmount,
        fat = fat,
        lr = lr,
        ts = ts,
        milkPrice = milkPrice,
        payment = payment,
        balance = balance,
        rateUsed = rateUsed,
        notes = notes
    )

fun Purchase.toEntity(): PurchaseEntity =
    PurchaseEntity(
        purchaseId = id,
        supplierId = supplierId,
        dateMillis = date.toMillis(),
        milkAmount = milkAmount,
        fat = fat,
        lr = lr,
        ts = ts,
        milkPrice = milkPrice,
        payment = payment,
        balance = balance,
        rateUsed = rateUsed,
        notes = notes
    )

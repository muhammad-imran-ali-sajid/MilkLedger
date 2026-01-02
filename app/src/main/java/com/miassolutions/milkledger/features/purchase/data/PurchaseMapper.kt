package com.miassolutions.milkledger.features.purchase.data

import com.miassolutions.milkledger.features.purchase.domain.Purchase
import com.miassolutions.milkledger.utils.extensions.toLocalDate
import com.miassolutions.milkledger.utils.extensions.toMillis

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

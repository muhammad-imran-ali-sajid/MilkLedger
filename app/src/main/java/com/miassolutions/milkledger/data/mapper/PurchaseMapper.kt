package com.miassolutions.milkledger.data.mapper

import com.miassolutions.milkledger.data.local.entities.PurchaseEntity
import com.miassolutions.milkledger.data.local.entities.SalesEntity
import com.miassolutions.milkledger.data.remote.model.FirestorePurchase
import com.miassolutions.milkledger.data.remote.model.FirestoreSales
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun PurchaseEntity.toFirestoreModel(): FirestorePurchase {
    return FirestorePurchase(
        purchaseId = purchaseId,
        supplierId = supplierId,
        date = date.toString(),
        milkAmount = milkAmount,
        fat = fat,
        lr = lr,
        ts = ts,
        milkPrice = milkPrice,
        payment = payment,
        balance = balance,
        rateUsed = rateUsed,
        notes = notes,
        isSynced = isSynced,
        updatedAt = updatedAt,
        deletedAt = deletedAt?.toString()
    )
}


fun FirestorePurchase.toEntityModel(): PurchaseEntity {
    return PurchaseEntity(
        purchaseId = purchaseId,
        supplierId = supplierId,
        date = LocalDate.parse(date),
        milkAmount = milkAmount,
        fat = fat,
        lr = lr,
        ts = ts,
        milkPrice = milkPrice,
        payment = payment,
        balance = balance,
        rateUsed = rateUsed,
        notes = notes,
        isSynced = isSynced,
        updatedAt = updatedAt,
        deletedAt = deletedAt.takeIf { it.isNullOrBlank() }?.let { LocalDateTime.parse(it) }
    )
}
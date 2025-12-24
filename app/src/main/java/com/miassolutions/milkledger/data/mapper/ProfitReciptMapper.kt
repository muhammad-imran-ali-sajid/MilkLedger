package com.miassolutions.milkledger.data.mapper

import com.miassolutions.milkledger.core.extensions.toLocalDate
import com.miassolutions.milkledger.core.extensions.toMillis
import com.miassolutions.milkledger.data.local.entities.ProfitReceiptEntity
import com.miassolutions.milkledger.domain.model.ProfitReceipt

fun ProfitReceiptEntity.toDomain(): ProfitReceipt =
    ProfitReceipt(
        id = receiptId,
        date = dateMillis.toLocalDate(),
        receivedFromId = receivedFromId,
        amount = amountReceived,
        note = note
    )

fun ProfitReceipt.toEntity(): ProfitReceiptEntity =
    ProfitReceiptEntity(
        receiptId = id,
        dateMillis = date.toMillis(),
        receivedFromId = receivedFromId,
        amountReceived = amount,
        note = note
    )

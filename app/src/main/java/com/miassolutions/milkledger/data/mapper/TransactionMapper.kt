package com.miassolutions.milkledger.data.mapper

import com.miassolutions.milkledger.core.extensions.toLocalDate
import com.miassolutions.milkledger.core.extensions.toMillis
import com.miassolutions.milkledger.data.local.entities.TransactionEntity
import com.miassolutions.milkledger.domain.model.Transaction

fun TransactionEntity.toDomain(): Transaction =
    Transaction(
        id = transactionId,
        date = dateMillis.toLocalDate(),
        type = type,
        referenceId = referenceId,
        debit = debit,
        credit = credit,
        profitImpact = profitImpact,
        note = note
    )

fun Transaction.toEntity(): TransactionEntity =
    TransactionEntity(
        transactionId = id,
        dateMillis = date.toMillis(),
        type = type,
        referenceId = referenceId,
        debit = debit,
        credit = credit,
        profitImpact = profitImpact,
        note = note
    )

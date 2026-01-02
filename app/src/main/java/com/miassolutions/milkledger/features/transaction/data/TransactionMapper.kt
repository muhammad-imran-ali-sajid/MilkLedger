package com.miassolutions.milkledger.features.transaction.data

import com.miassolutions.milkledger.utils.extensions.toLocalDate
import com.miassolutions.milkledger.utils.extensions.toMillis

fun TransactionEntity.toDomain(): Transaction =
    Transaction(
        id = transactionId,
        date = dateMillis.toLocalDate(),
        type = type,
        referenceId = referenceId,
        accountId = accountId,
        debit = debit,
        credit = credit,
        profitImpact = profitImpact,
        notes = note
    )

fun Transaction.toEntity(): TransactionEntity =
    TransactionEntity(
        transactionId = id,
        dateMillis = date.toMillis(),
        type = type,
        referenceId = referenceId,
        accountId = accountId,
        debit = debit,
        credit = credit,
        profitImpact = profitImpact,
        note = notes
    )

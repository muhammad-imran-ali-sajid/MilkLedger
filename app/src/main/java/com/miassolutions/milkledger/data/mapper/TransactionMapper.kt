package com.miassolutions.milkledger.data.mapper

import com.miassolutions.milkledger.utils.extensions.toLocalDate
import com.miassolutions.milkledger.utils.extensions.toMillis
import com.miassolutions.milkledger.data.local.entities.TransactionEntity
import com.miassolutions.milkledger.domain.model.Transaction

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
        notes = notes
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
        notes = notes
    )

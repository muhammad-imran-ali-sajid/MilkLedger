package com.miassolutions.milkledger.domain.model

import com.miassolutions.milkledger.data.local.entities.TransactionType
import java.time.LocalDate

data class Transaction(
    val id: String,
    val date: LocalDate,
    val type: TransactionType,
    val referenceId: String?,
    val accountId: String?,
    val debit: Double,
    val credit: Double,
    val profitImpact: Double,
    val notes: String?
)

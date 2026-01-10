package com.miassolutions.milkledger.features.account.model

import com.miassolutions.milkledger.core.localdb.account.local.AccountType
import java.time.LocalDate

data class AccountUi(
    val id: String,
    val name: String,
    val type: AccountType,
    val sortOrder: Int,
    val initialBalance: Long?,
    val openingDate: LocalDate,
    val defaultRate: Double,
    val advanceAmount: Long?,
)
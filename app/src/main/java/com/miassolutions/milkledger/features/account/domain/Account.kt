package com.miassolutions.milkledger.features.account.domain

import com.miassolutions.milkledger.core.localdb.account.local.AccountType
import java.time.LocalDate

data class Account(
    val createdDate: LocalDate?,
    val accountId: String = "",
    val name: String,
    val phone: String? = null,
    val type: AccountType,

    val sortOrder: Int = 0,
    val defaultRate: Double,

    val isActive: Boolean,
    val currentBalance: Long?,

    // Sirf Profile me show hoga (No Ledger Effect)
    val advanceAmount: Long?,

    // Ye Ledger me "Opening Balance" k tor par jayega (Agar user dale)
    val initialBalance: Long?,
)
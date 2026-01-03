package com.miassolutions.milkledger.features.account


import com.miassolutions.milkledger.core.localdb.account.AccountType

data class Account(
    val accountId: String = "",
    val name: String,
    val phone: String?,
    val type: AccountType,

    val sortOrder: Int = 0,
    val defaultRate: Double = 0.0,

    // Sirf Profile me show hoga (No Ledger Effect)
    val advanceAmount: Long = 0,

    // Ye Ledger me "Opening Balance" k tor par jayega (Agar user dale)
    val initialBalance: Long = 0,
)
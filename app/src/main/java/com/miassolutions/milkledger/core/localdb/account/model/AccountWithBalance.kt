package com.miassolutions.milkledger.core.localdb.account.model

import com.miassolutions.milkledger.core.localdb.account.local.AccountType

data class AccountWithBalance(
    val accountId: String,
    val name: String,
    val type: AccountType,
    val balance: Long // Calculated field
)
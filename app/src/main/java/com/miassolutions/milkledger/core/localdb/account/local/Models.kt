package com.miassolutions.milkledger.core.localdb.account.local

data class AccountWithBalance(
    val accountId: String,
    val name: String,
    val type: AccountType,
    val balance: Long // Calculated field
)
package com.miassolutions.milkledger.features.account.model

import androidx.annotation.DrawableRes
import com.miassolutions.milkledger.core.localdb.account.local.AccountType

data class AccountUi(
    val id: String,
    val personName: String,
    val accountType: AccountType,
    val sortOrder: Int,
    val initialBalance: Long?,
    val defaultRate: Double,
    val advanceAmount: Long?,
    @DrawableRes val bgDrawable: Int
)
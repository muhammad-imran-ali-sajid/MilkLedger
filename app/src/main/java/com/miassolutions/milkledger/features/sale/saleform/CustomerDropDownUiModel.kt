package com.miassolutions.milkledger.features.sale.saleform

import com.miassolutions.milkledger.features.account.domain.Account

data class CustomerDropDownUiModel(
    val account: Account, // Apki existing Account class
    val isEntryDoneToday: Boolean = false
)
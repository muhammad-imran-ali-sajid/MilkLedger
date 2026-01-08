package com.miassolutions.milkledger.features.purchase.model

import com.miassolutions.milkledger.features.account.domain.Account

data class SupplierDropDownUiModel(
    val account: Account,
    val isEntryDoneToday: Boolean
) {
    // ⚠️ Zaroori: ToString ko override karein taake AutoCompleteTextView me Sahi naam nazar aaye
    override fun toString(): String {
        return account.name
    }
}
package com.miassolutions.milkledger.features.account.mapper


import com.miassolutions.milkledger.R
import com.miassolutions.milkledger.core.localdb.account.local.AccountType
import com.miassolutions.milkledger.features.account.domain.Account
import com.miassolutions.milkledger.features.account.form.AccountFormUiState
import com.miassolutions.milkledger.features.account.model.AccountUI
import java.util.UUID

fun Account.toUi(): AccountUI =
    AccountUI(
        id = accountId,
        personName = name,
        accountType = type,
        sortOrder = sortOrder,
        initialBalance = initialBalance,
        defaultRate = defaultRate,
        advanceAmount = advanceAmount,
        bgDrawable = when (type) {
            AccountType.CUSTOMER -> R.drawable.bg_sale
            else -> R.drawable.bg_purchase

        }
    )


fun Account.toFormUiState(): AccountFormUiState =
    AccountFormUiState(
        sortOrder = sortOrder.toString(),
        personName = name,
        selectAccountType = type,
        rate = defaultRate.toString(),
        initialBalance = initialBalance.toString(),
        advanceAmount = advanceAmount.toString().orEmpty(),
    )


fun AccountFormUiState.toDomain(
    existingId: String? = null
): Account =
    Account(
        accountId = existingId ?: UUID.randomUUID().toString(),
        name = personName,

        type = selectAccountType,
        sortOrder = sortOrder.toInt(),
        defaultRate = rate.toDouble(),
        advanceAmount = advanceAmount.toLongOrNull(),
        initialBalance = initialBalance.toLongOrNull(),

        )
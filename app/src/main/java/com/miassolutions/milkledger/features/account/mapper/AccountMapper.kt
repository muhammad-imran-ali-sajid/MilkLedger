package com.miassolutions.milkledger.features.account.mapper


import android.content.Context
import com.miassolutions.milkledger.R
import com.miassolutions.milkledger.core.localdb.account.local.AccountType
import com.miassolutions.milkledger.features.account.domain.Account
import com.miassolutions.milkledger.features.account.form.AccountFormUiState
import com.miassolutions.milkledger.features.account.model.AccountUi
import com.miassolutions.milkledger.utils.extensions.toPaisa
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

fun Account.toUi(): AccountUi =
    AccountUi(
        id = accountId,
        personName = name,
        accountType = type,
        sortOrder = sortOrder,
        initialBalance = initialBalance,
        defaultRate = defaultRate,
        advanceAmount = advanceAmount,
        bgDrawable = when (type) {
            AccountType.CUSTOMER -> R.color.green_200
            else -> R.color.blue_100

        }
    )

fun List<Account>.toUiList(): List<AccountUi> = this.map { it.toUi() }

fun Flow<List<Account>>.toUiListFlow(): Flow<List<AccountUi>> = this.map { it.toUiList() }


fun Account.toFormUiState(): AccountFormUiState =
    AccountFormUiState(
        sortOrder = sortOrder.toString(),
        personName = name,
        selectAccountType = type,
        rate = defaultRate.toString(),
        initialBalance = initialBalance.toString().orEmpty(),
        advanceAmount = advanceAmount.toString().orEmpty(),
    )


fun AccountFormUiState.toDomain(
    existingId: String
): Account =
    Account(
        accountId = existingId,
        name = personName,

        type = selectAccountType,
        sortOrder = sortOrder.toInt(),
        defaultRate = rate.toDouble(),
        advanceAmount = advanceAmount.toPaisa(),
        initialBalance = initialBalance.toPaisa(),

        )

fun AccountType.title(context: Context): String =
    context.getString(
        when (this) {
            AccountType.CUSTOMER -> R.string.customers
            AccountType.SUPPLIER -> R.string.suppliers
            AccountType.OWNER -> throw IllegalArgumentException("OWNER has no title")
        }
    )
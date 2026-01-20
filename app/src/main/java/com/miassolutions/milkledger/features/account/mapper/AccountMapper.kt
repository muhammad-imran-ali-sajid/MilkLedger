package com.miassolutions.milkledger.features.account.mapper


import android.content.Context
import com.miassolutions.milkledger.R
import com.miassolutions.milkledger.core.localdb.account.local.AccountType
import com.miassolutions.milkledger.features.account.domain.Account
import com.miassolutions.milkledger.features.account.form.AccountFormUiState
import com.miassolutions.milkledger.features.account.model.AccountUi
import com.miassolutions.milkledger.utils.extensions.toLocalDate
import com.miassolutions.milkledger.utils.extensions.toMillis
import com.miassolutions.milkledger.utils.extensions.toPaisa
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.util.UUID

fun Account.toUi(): AccountUi =
    AccountUi(
        id = accountId,
        name = name,
        type = type,
        sortOrder = sortOrder,
        initialBalance = initialBalance,
        openingDate = createdDate?: LocalDate.now(),
        defaultRate = defaultRate,
        advanceAmount = advanceAmount,
        currentBalance =currentBalance

    )

fun List<Account>.toUiList(): List<AccountUi> = this.map { it.toUi() }






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
        isActive = isActive,
        currentBalance = null,
        createdDate = openingDate,
    )

fun AccountType.title(context: Context): String =
    context.getString(
        when (this) {
            AccountType.CUSTOMER -> R.string.customers
            AccountType.SUPPLIER -> R.string.suppliers
            AccountType.OWNER -> throw IllegalArgumentException("OWNER has no title")
        }
    )
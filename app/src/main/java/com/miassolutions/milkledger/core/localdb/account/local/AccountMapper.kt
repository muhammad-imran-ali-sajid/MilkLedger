package com.miassolutions.milkledger.core.localdb.account.local

import com.miassolutions.milkledger.features.account.domain.Account
import com.miassolutions.milkledger.utils.extensions.toLocalDate
import com.miassolutions.milkledger.utils.extensions.toMillis
import java.util.UUID


fun AccountEntity.toDomain(): Account {
    return Account(
        createdDate = this.createdAtMillis.toLocalDate(),
        accountId = this.accountId,
        name = this.name,
        phone = this.phone,
        type = this.accountType,
        sortOrder = this.sortOrder,
        isActive = this.isActive,
        defaultRate = this.defaultRate,
        advanceAmount = this.advanceAmount,
        initialBalance = this.initialBalance,
    )
}

fun Account.toEntity(): AccountEntity {
    return AccountEntity(
        accountId = this.accountId.ifBlank { UUID.randomUUID().toString() },
        name = this.name,
        phone = this.phone,
        accountType = this.type,
        sortOrder = this.sortOrder,
        defaultRate = this.defaultRate,
        advanceAmount = this.advanceAmount,
        initialBalance = this.initialBalance,
        createdAtMillis = this.createdDate.toMillis()
    )
}
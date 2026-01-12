package com.miassolutions.milkledger.features.account.domain.usecase

import com.miassolutions.milkledger.features.account.data.AccountRepository
import com.miassolutions.milkledger.features.account.domain.Account
import java.time.LocalDate
import javax.inject.Inject

class LoadAccountForEditUseCase @Inject constructor(
    private val repository: AccountRepository
) {

    suspend operator fun invoke(accountId: String): Pair<Account, LocalDate?> {

        val account = repository.getAccountById(accountId)
            ?: error("Account not found")

        val openingDate =
            repository.getOpeningDate(accountId)
                ?: account.createdDate

        return account to openingDate
    }
}

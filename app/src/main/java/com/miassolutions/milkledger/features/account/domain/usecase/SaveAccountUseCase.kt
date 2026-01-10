package com.miassolutions.milkledger.features.account.domain.usecase


import com.miassolutions.milkledger.features.account.data.AccountRepository
import com.miassolutions.milkledger.features.account.domain.Account
import java.time.LocalDate
import javax.inject.Inject

class SaveAccountUseCase @Inject constructor(
    private val repository: AccountRepository
) {

    suspend operator fun invoke(
        account: Account,
        openingDate: LocalDate,
        excludeId: String?
    ): SaveAccountResult {

        if (
            repository.isSortOrderExist(
                account.sortOrder,
                account.type,
                excludeId
            )
        ) {
            return SaveAccountResult.SortOrderExists
        }

        repository.saveAccount(account, openingDate)
        return SaveAccountResult.Success
    }
}

sealed interface SaveAccountResult {
    object Success : SaveAccountResult
    object SortOrderExists : SaveAccountResult
}

package com.miassolutions.milkledger.features.account.domain.usecase

import com.miassolutions.milkledger.features.account.data.AccountRepository
import javax.inject.Inject

class DeleteAccountUseCase @Inject constructor(
    private val repository: AccountRepository
) {

    suspend operator fun invoke(accountId: String): DeleteAccountResult {

        val balance = repository.getCurrentBalance(accountId)

        return if (balance != 0L) {
            DeleteAccountResult.BalanceNotZero(balance)
        } else {
            repository.deleteAccount(accountId)
            DeleteAccountResult.Deleted
        }
    }
}

sealed interface DeleteAccountResult {
    data class BalanceNotZero(val balance: Long) : DeleteAccountResult
    object Deleted : DeleteAccountResult
}

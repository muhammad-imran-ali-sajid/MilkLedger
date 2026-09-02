package com.miassolutions.milkledger.features.insights

import com.miassolutions.milkledger.core.localdb.account.local.AccountType
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class InsightRepository @Inject constructor(
    private val dao: InsightsDao
) {
    fun getBalanceList(accountType: AccountType = AccountType.CUSTOMER): Flow<List<BalanceWithAccountType>> {
        return dao.getBalanceList(accountType)
    }
}
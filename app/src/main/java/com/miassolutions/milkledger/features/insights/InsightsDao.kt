package com.miassolutions.milkledger.features.insights

import androidx.room.Dao
import androidx.room.Query
import com.miassolutions.milkledger.core.localdb.account.local.AccountType
import kotlinx.coroutines.flow.Flow

@Dao
interface InsightsDao {

    @Query(
        """
        SELECT a.name AS name,
        CASE
        WHEN a.accountType = 'CUSTOMER' THEN (SUM(f.debit)-SUM(f.credit))/100
        ELSE (SUM(f.credit)-SUM(f.debit))/100
        END AS balance
        FROM financial_ledger_table As f
        INNER JOIN accounts_table AS a
        ON f.accountId = a.accountId
        WHERE a.accountType = :accountType
         GROUP BY a.name
    """
    )
    fun getBalanceList(accountType: AccountType): Flow<List<BalanceWithAccountType>>
}

data class BalanceWithAccountType(
    val name: String,
    val balance: Long
)
package com.miassolutions.milkledger.core.localdb.account.local

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.miassolutions.milkledger.core.contstants.Constants.OWNER_ACCOUNT_ID
import com.miassolutions.milkledger.core.localdb.account.model.AccountWithBalance
import kotlinx.coroutines.flow.Flow


@Dao
interface AccountDao {
    
    @Query("SELECT * FROM accounts_table")
    suspend fun getAllAccountsForBackup(): List<AccountEntity>

    // 🔥🔥 NEW OPTIMIZED QUERY (The Magic) 🔥🔥
    // Account List + Live Balance + Opening Date (Aik sath)
    @Query("""
        SELECT 
            a.*,
            
            -- 1. Calculate Current Balance (Total Debit - Total Credit)
            (
                SELECT (TOTAL(l.debit) - TOTAL(l.credit))
                FROM financial_ledger_table l
                WHERE l.accountId = a.accountId 
                AND l.deletedAtMillis IS NULL
            ) as currentBalance,
            
            -- 2. Fetch Opening Date
            (
                SELECT l_open.dateMillis
                FROM financial_ledger_table l_open
                WHERE l_open.accountId = a.accountId 
                AND l_open.type = 'OPENING_BALANCE'
                AND l_open.deletedAtMillis IS NULL
                LIMIT 1
            ) as openingDateMillis
            
        FROM accounts_table a
        WHERE a.accountType = :type
        AND a.deletedAtMillis IS NULL
        ORDER BY a.sortOrder ASC
    """)
    fun getAccountsWithStats(type: AccountType): Flow<List<AccountWithStats>>


    // --- Baki Purani Queries ---

    @Query("SELECT COUNT(*) FROM accounts_table WHERE accountId = :id")
    suspend fun isAccountExist(id: String): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIgnore(account: AccountEntity)

    @Query(
        """
        SELECT 
        a.accountId,
        a.name,
        a.accountType AS type,
        (
            SELECT(TOTAL(debit) - TOTAL(credit))
            FROM financial_ledger_table
            WHERE accountId = a.accountId
            AND deletedAtMillis IS NULL
        ) AS balance
        FROM accounts_table a
        WHERE a.deletedAtMillis IS NULL
        ORDER BY a.name ASC
    """
    )
    fun getAllAccountsWithBalance(): Flow<List<AccountWithBalance>>

    @Query(
        """
        SELECT * FROM accounts_table
        WHERE accountId = :ownerId
        AND deletedAtMillis IS NULL
        LIMIT 1
    """
    )
    suspend fun getOwner(ownerId: String = OWNER_ACCOUNT_ID): AccountEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(account: AccountEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(account: AccountEntity): Long

    @Query(
        """SELECT EXISTS(
        SELECT 1 FROM accounts_table 
        WHERE sortOrder =:sortOrder
        AND accountType = :accountType
        AND (:excludeId IS NULL OR accountId != :excludeId)
)"""
    )
    suspend fun isSortOrderExist(
        sortOrder: Int,
        accountType: AccountType,
        excludeId: String?
    ): Boolean

    @Update
    suspend fun update(account: AccountEntity)

    // Simple list (bina balance k)
    @Query("SELECT * FROM accounts_table WHERE accountType = :type AND deletedAtMillis IS NULL ORDER BY sortOrder ASC")
    fun getAccountsByType(type: AccountType): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts_table WHERE accountId = :id")
    suspend fun getAccountById(id: String): AccountEntity?

    @Query("UPDATE accounts_table SET deletedAtMillis = :time WHERE accountId = :id")
    suspend fun softDelete(id: String, time: Long)

    @Query("UPDATE accounts_table SET deletedAtMillis = NULL WHERE accountId = :id")
    suspend fun restore(id: String)

    @Query("DELETE FROM accounts_table WHERE deletedAtMillis IS NOT NULL")
    suspend fun permanentlyDeleteAllAccounts()
}


data class AccountWithStats(
    @Embedded val account: AccountEntity,

    // Sub-Queries se aany wala data
    val currentBalance: Long?,
    val openingDateMillis: Long?
)
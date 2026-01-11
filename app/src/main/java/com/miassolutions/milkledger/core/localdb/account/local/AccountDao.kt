package com.miassolutions.milkledger.core.localdb.account.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.miassolutions.milkledger.core.contstants.Constants.OWNER_ACCOUNT_ID
import com.miassolutions.milkledger.core.localdb.account.model.AccountWithBalance
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {


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

    // 1. Naya Account Banane ke liye
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

    // 2. Edit karne ke liye
    @Update
    suspend fun update(account: AccountEntity)

    // 3. Customer List dikhane ke liye (Reactive Flow)
    // Sirf wo accounts layen jo delete nahi hue
    @Query("SELECT * FROM accounts_table WHERE accountType = :type AND deletedAtMillis IS NULL ORDER BY sortOrder ASC")
    fun getAccountsByType(type: AccountType): Flow<List<AccountEntity>>

    // 4. Dropdown ya Detail ke liye single account
    @Query("SELECT * FROM accounts_table WHERE accountId = :id")
    suspend fun getAccountById(id: String): AccountEntity?

    // 5. Soft Delete Logic
    @Query("UPDATE accounts_table SET deletedAtMillis = :time WHERE accountId = :id")
    suspend fun softDelete(id: String, time: Long)

    @Query("UPDATE accounts_table SET deletedAtMillis = NULL WHERE accountId = :id")
    suspend fun restore(id: String)

    @Query("DELETE FROM accounts_table WHERE deletedAtMillis IS NOT NULL")
    suspend fun permanentlyDeleteAllAccounts()


}
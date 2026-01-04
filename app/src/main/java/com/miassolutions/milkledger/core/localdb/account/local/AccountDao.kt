package com.miassolutions.milkledger.core.localdb.account.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {

    // 1. Naya Account Banane ke liye
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(account: AccountEntity): Long

    @Query("SELECT EXISTS(SELECT 1 FROM accounts_table WHERE sortOrder =:sortOrder  AND accountType = :accountType)")
    suspend fun isSortOrderExist(sortOrder: Int, accountType: AccountType) : Boolean

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
}
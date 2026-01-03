package com.miassolutions.milkledger.core.localdb.account

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
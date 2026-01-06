package com.miassolutions.milkledger.core.localdb.ledger

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface LedgerDao {

    // ✅ Cleanest Way: Specific Date tak ka Balance
    // COALESCE(SUM(...), 0) ka matlab hai agar koi entry na ho to 0 return kro (Crash se bachne k liye)

    @Query("""
        SELECT (COALESCE(SUM(debit), 0) - COALESCE(SUM(credit), 0))
        FROM financial_ledger_table
        WHERE accountId = :accountId
        AND dateMillis <= :targetDate 
        AND deletedAtMillis IS NULL
    """)
    suspend fun getBalanceAsOfDate(accountId: String, targetDate: Long): Long

    // Balance calculation hamesha shuru se hoti hai (ORDER BY dateMillis ASC)
    @Query("""
        SELECT * FROM financial_ledger_table 
        WHERE accountId = :accountId 
        AND deletedAtMillis IS NULL
        ORDER BY dateMillis ASC, createdAtMillis ASC
    """)
    fun getLedgerForRunningBalance(accountId: String): Flow<List<FinancialLedgerEntity>>

    //  Account save/update karte waqt purana balance hatana zaroori hai
    @Query("DELETE FROM financial_ledger_table WHERE accountId = :accountId AND type = 'OPENING_BALANCE'")
    suspend fun deleteOpeningBalance(accountId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(ledgerEntries: List<FinancialLedgerEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(ledger: FinancialLedgerEntity)

    @Update
    suspend fun update(ledger: FinancialLedgerEntity)

    // Reference ID se dhoondna (Update/Delete logic ke liye zaroori hai)
    @Query("SELECT * FROM financial_ledger_table WHERE referenceId = :refId LIMIT 1")
    suspend fun getByReferenceId(refId: String): FinancialLedgerEntity?

    // Customer ka Balance nikalna (Most Important)
    @Query("""
        SELECT (TOTAL(debit) - TOTAL(credit)) 
        FROM financial_ledger_table 
        WHERE accountId = :accountId 
        AND deletedAtMillis IS NULL
    """)
    fun getAccountBalance(accountId: String): Flow<Long>

    @Query("""
        UPDATE financial_ledger_table 
        SET deletedAtMillis = :time, isSynced = 0 
        WHERE referenceId = :refId
    """)
    suspend fun softDeleteByReference(refId: String, time: Long)

    // Customer ki last 50 transactions
    @Query("""
        SELECT * FROM financial_ledger_table 
        WHERE accountId = :accountId 
        AND deletedAtMillis IS NULL
        ORDER BY dateMillis DESC 
        LIMIT 50
    """)
    fun getLedgerHistory(accountId: String): Flow<List<FinancialLedgerEntity>>
}
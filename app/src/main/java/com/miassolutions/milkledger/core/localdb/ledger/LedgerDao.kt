package com.miassolutions.milkledger.core.localdb.ledger

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface LedgerDao {



    // ID aur Type ki bunyad par Ledger dhoondna (Update k liye zaroori hai)
    @Query("SELECT * FROM financial_ledger_table WHERE referenceId = :refId AND type = :type LIMIT 1")
    suspend fun getLedgerByReferenceId(refId: String, type: LedgerEntryType): FinancialLedgerEntity?


    // Reference ID ki bunyad par delete (Is se Sale aur Payment dono delete ho jayengi)
    @Query("UPDATE financial_ledger_table SET deletedAtMillis = :deleteTime WHERE referenceId = :refId")
    suspend fun     softDeleteLedgerByReference(refId: String, deleteTime: Long)

    @Delete
    suspend fun delete(entity: FinancialLedgerEntity)

    @Query("SELECT * FROM financial_ledger_table WHERE ledgerId = :ledgerId LIMIT 1")
    suspend fun getLedgerById(ledgerId: String) : FinancialLedgerEntity?


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


    // ----------------------------------------------------------------
    // 🔥 OWNER DASHBOARD QUERIES
    // ----------------------------------------------------------------

    // 1. Calculate NET PROFIT for Date Range
    // Formula: (Profit from Sales/Purchases) - (Business Expenses)
    // Note: 'profitImpact' column humne isi liye banaya tha.
    // Bus us column ka SUM le lein, jahan type Owner Drawing na ho.
    @Query("""
        SELECT COALESCE(SUM(profitImpact), 0) 
        FROM financial_ledger_table 
        WHERE dateMillis BETWEEN :start AND :end 
        AND deletedAtMillis IS NULL
        AND type != 'OWNER_DRAWING' 
        AND type != 'OWNER_WITHDRAWAL' -- Safety check agar naming change ho
    """)
    fun getNetProfitInRange(start: Long, end: Long): Flow<Long>

    // 2. Calculate TOTAL DRAWINGS for Date Range
    // Formula: Sum of all OWNER_DRAWING (Debit side represents money taken)
    @Query("""
        SELECT COALESCE(SUM(debit), 0) 
        FROM financial_ledger_table 
        WHERE dateMillis BETWEEN :start AND :end 
        AND deletedAtMillis IS NULL
        AND type = 'OWNER_DRAWING' 
    """)
    fun getTotalDrawingsInRange(start: Long, end: Long): Flow<Long>

    // 3. Get LIST of Owner Transactions
    // Sirf wo entries jahan type = OWNER_DRAWING (Cash Withdrawals + Personal Expenses)
    @Query("""
        SELECT * FROM financial_ledger_table 
        WHERE dateMillis BETWEEN :start AND :end 
        AND deletedAtMillis IS NULL
        AND type = 'OWNER_DRAWING'
        ORDER BY dateMillis DESC, createdAtMillis DESC
    """)
    fun getOwnerTransactionsInRange(start: Long, end: Long): Flow<List<FinancialLedgerEntity>>


}
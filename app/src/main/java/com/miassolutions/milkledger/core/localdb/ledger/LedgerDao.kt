package com.miassolutions.milkledger.core.localdb.ledger

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.miassolutions.milkledger.features.cashflow.CashflowSummary
import com.miassolutions.milkledger.features.owner.domain.DailyProfitTuple
import kotlinx.coroutines.flow.Flow

@Dao
interface LedgerDao {

    @Query("""    
        SELECT (COALESCE(SUM(debit), 0) - COALESCE(SUM(credit), 0)) 
        FROM financial_ledger_table 
        WHERE accountId = :accountId 
        AND deletedAtMillis IS NULL
    """)
    suspend fun getAccountNetBalance(accountId: String): Long?

    @Query("SELECT * FROM financial_ledger_table WHERE accountId = :accId AND type = 'OPENING_BALANCE' LIMIT 1")
    suspend fun getOpeningBalanceEntry(accId: String): FinancialLedgerEntity?


    // ID aur Type ki bunyad par Ledger dhoondna (Update k liye zaroori hai)
    @Query("SELECT * FROM financial_ledger_table WHERE referenceId = :refId AND type = :type LIMIT 1")
    suspend fun getLedgerByReferenceId(refId: String, type: LedgerEntryType): FinancialLedgerEntity?


    // Reference ID ki bunyad par delete (Is se Sale aur Payment dono delete ho jayengi)
    @Query("UPDATE financial_ledger_table SET deletedAtMillis = :deleteTime WHERE referenceId = :refId")
    suspend fun softDeleteLedgerByReference(refId: String, deleteTime: Long)

    @Query("UPDATE financial_ledger_table SET deletedAtMillis = :currentTime, isSynced = 0 WHERE ledgerId = :id")
    suspend fun softDeleteLedgerById(id: String, currentTime: Long)

    @Delete
    suspend fun delete(entity: FinancialLedgerEntity)

    @Query("SELECT * FROM financial_ledger_table WHERE ledgerId = :ledgerId LIMIT 1")
    suspend fun getLedgerById(ledgerId: String): FinancialLedgerEntity?


    // Balance calculation hamesha shuru se hoti hai (ORDER BY dateMillis ASC)
    @Query(
        """
        SELECT * FROM financial_ledger_table 
        WHERE accountId = :accountId 
        AND deletedAtMillis IS NULL
        ORDER BY dateMillis ASC, createdAtMillis ASC
    """
    )
    fun getLedgerForRunningBalance(accountId: String): Flow<List<FinancialLedgerEntity>>

    //  Account save/update karte waqt purana balance hatana zaroori hai
    @Query("DELETE FROM financial_ledger_table WHERE accountId = :accountId AND type = 'OPENING_BALANCE'")
    suspend fun deleteOpeningBalance(accountId: String)

    @Query("UPDATE financial_ledger_table SET deletedAtMillis = :time, isSynced = 0 WHERE accountId = :accountId AND type = :type")
    suspend fun softDeleteOpeningBalance(
        accountId: String,
        time: Long,
        type: LedgerEntryType = LedgerEntryType.OPENING_BALANCE // Default Value
    )

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
    @Query(
        """
        SELECT (TOTAL(debit) - TOTAL(credit)) 
        FROM financial_ledger_table 
        WHERE accountId = :accountId 
        AND deletedAtMillis IS NULL
    """
    )
    fun getAccountBalance(accountId: String): Flow<Long>

    @Query(
        """
        UPDATE financial_ledger_table 
        SET deletedAtMillis = :time, isSynced = 0 
        WHERE referenceId = :refId
    """
    )
    suspend fun softDeleteByReference(refId: String, time: Long)

    // Customer ki last 50 transactions
    @Query(
        """
        SELECT * FROM financial_ledger_table 
        WHERE accountId = :accountId 
        AND deletedAtMillis IS NULL
        ORDER BY dateMillis DESC 
        LIMIT 50
    """
    )
    fun getLedgerHistory(accountId: String): Flow<List<FinancialLedgerEntity>>


    // 🔥 1. Balance up to specific date
    @Query(
        """
    SELECT (TOTAL(debit) - TOTAL(credit)) 
    FROM financial_ledger_table 
    WHERE accountId = :accountId 
    AND deletedAtMillis IS NULL
    AND dateMillis <= :dateLimit  -- 🔥 Ye line naya magic karegi
"""
    )
    fun getAccountBalanceUntil(accountId: String, dateLimit: Long): Flow<Long>


    // 🔥 2. History up to specific date
    @Query(
        """
    SELECT * FROM financial_ledger_table 
    WHERE accountId = :accountId 
    AND deletedAtMillis IS NULL
    AND dateMillis <= :dateLimit  -- 🔥 Sirf us din tak ka data
    ORDER BY dateMillis DESC 
    LIMIT 50
"""
    )
    fun getLedgerHistoryUntil(accountId: String, dateLimit: Long): Flow<List<FinancialLedgerEntity>>


    // ----------------------------------------------------------------
    // 🔥 OWNER DASHBOARD QUERIES
    // ----------------------------------------------------------------

    // 1. Calculate NET PROFIT for Date Range
    // Formula: (Profit from Sales/Purchases) - (Business Expenses)
    // Note: 'profitImpact' column humne isi liye banaya tha.
    // Bus us column ka SUM le lein, jahan type Owner Drawing na ho.
    @Query(
        """
        SELECT COALESCE(SUM(profitImpact), 0) 
        FROM financial_ledger_table 
        WHERE dateMillis BETWEEN :start AND :end 
        AND deletedAtMillis IS NULL
        AND type != 'OWNER_DRAWING' 
        AND type != 'OWNER_WITHDRAWAL' -- Safety check agar naming change ho
    """
    )
    fun getNetProfitInRange(start: Long, end: Long): Flow<Long>

    // 2. Calculate TOTAL DRAWINGS for Date Range
    // Formula: Sum of all OWNER_DRAWING (Debit side represents money taken)
    @Query(
        """
        SELECT COALESCE(SUM(debit), 0) 
        FROM financial_ledger_table 
        WHERE dateMillis BETWEEN :start AND :end 
        AND deletedAtMillis IS NULL
        AND type = 'OWNER_DRAWING' 
    """
    )
    fun getTotalDrawingsInRange(start: Long, end: Long): Flow<Long>

    // 3. Get LIST of Owner Transactions
    // Sirf wo entries jahan type = OWNER_DRAWING (Cash Withdrawals + Personal Expenses)
    @Query(
        """
        SELECT * FROM financial_ledger_table 
        WHERE dateMillis BETWEEN :start AND :end 
        AND deletedAtMillis IS NULL
        AND type = 'OWNER_DRAWING'
        ORDER BY dateMillis DESC, createdAtMillis DESC
    """
    )
    fun getOwnerTransactionsInRange(start: Long, end: Long): Flow<List<FinancialLedgerEntity>>


    // 🔥 Daily Profit Breakdown
    // Hum ProfitImpact ko sum karenge, Din k hisaab se group kar k.
    // Condition: Sirf wo entries jinka profit impact hai (transaction delete na ho).
    @Query("""
        SELECT 
            dateMillis, 
            SUM(profitImpact) as dailyTotal
        FROM financial_ledger_table
        WHERE dateMillis BETWEEN :start AND :end
        AND deletedAtMillis IS NULL
        AND profitImpact != 0
        GROUP BY dateMillis
        ORDER BY dateMillis DESC
    """)
    fun getDailyProfitBreakdown(start: Long, end: Long): Flow<List<DailyProfitTuple>>



    @Query("""
        SELECT 
            -- 🟢 CASH IN: Sirf jab Cash Receive hua ho
            COALESCE(SUM(
                CASE 
                    WHEN type = 'CASH_RECEIVED' THEN credit 
                    ELSE 0 
                END
            ), 0) as totalIn, 

            -- 🔴 CASH OUT: Jab Payment ki, Kharcha hua, ya Malik ne nikala
            COALESCE(SUM(
                CASE 
                    WHEN type IN ('CASH_PAID', 'BUSINESS_EXPENSE', 'OWNER_DRAWING') THEN debit 
                    ELSE 0 
                END
            ), 0) as totalOut

        FROM financial_ledger_table
        WHERE dateMillis BETWEEN :start AND :end
        AND deletedAtMillis IS NULL
    """)
    fun getCashflowSummary(start: Long, end: Long): Flow<CashflowSummary>

    // 🔥 Updated: Sirf Cash transactions laane ke liye
    @Query("""
        SELECT * FROM financial_ledger_table
        WHERE dateMillis BETWEEN :start AND :end
        AND deletedAtMillis IS NULL
        AND type IN ('CASH_RECEIVED', 'CASH_PAID', 'BUSINESS_EXPENSE', 'OWNER_DRAWING')
        ORDER BY dateMillis DESC, createdAtMillis DESC
    """)
    fun getLedgerEntriesInRange(start: Long, end: Long): Flow<List<FinancialLedgerEntity>>


}
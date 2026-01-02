package com.miassolutions.milkledger.features.transaction.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.miassolutions.milkledger.features.sale.domain.model.CustomerLedgerRow
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {



    // 1️⃣ TOTAL BUSINESS EXPENSES (Dukan ka Kharcha)
    // Wo expenses jinse profit kam hua (profitImpact < 0)
    @Query("""
        SELECT SUM(debit) FROM transaction_table 
        WHERE type = 'EXPENSE' 
        AND profitImpact < 0 
        AND deletedAtMillis IS NULL
    """)
    fun getTotalBusinessExpenses(): Flow<Double?>

    // 2️⃣ TOTAL PERSONAL EXPENSES (Ghar ka Kharcha)
    // Wo expenses jinka profit par asar nahi hua (profitImpact = 0)
    @Query("""
        SELECT SUM(debit) FROM transaction_table 
        WHERE type = 'EXPENSE' 
        AND profitImpact = 0 
        AND deletedAtMillis IS NULL
    """)
    fun getTotalPersonalExpenses(): Flow<Double?>



    // ----------------------------------------------------
    // WRITE OPERATIONS
    // ----------------------------------------------------



    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertAll(transactions: List<TransactionEntity>)

    /**
     * Sale/Expense delete honay par uski Transaction ko bhi soft-delete karna zaroori hai.
     * Hum referenceId (SaleId) use karte hain.
     */
    @Query("""
        UPDATE transaction_table 
        SET deletedAtMillis = :deletedAt, isSynced = 0 
        WHERE referenceId = :refId
    """)
    suspend fun softDeleteByReference(refId: String, deletedAt: Long)

    // ----------------------------------------------------
    // LEDGER (KHATA) QUERIES
    // ----------------------------------------------------

    /**
     * Customer/Supplier ka poora hisab.
     * ORDER BY ASC zaroori hai taake Running Balance sahi calculate ho.
     * Tie-breaker: Agar date same ho, to createdAt pehle dekho.
     */
    @Query("""
        SELECT * FROM transaction_table 
        WHERE accountId = :accountId 
        AND deletedAtMillis IS NULL 
        ORDER BY dateMillis ASC, createdAtMillis ASC
    """)
    fun getAccountLedger(accountId: String): Flow<List<TransactionEntity>>

    /**
     * Kisi aik banday ka Total Balance (Udhaar).
     * Formula: (Credit - Debit).
     * Positive = Usne dene hain (Receivable).
     * Negative = Humne dene hain (Payable).
     */
    @Query("""
        SELECT SUM(credit - debit) 
        FROM transaction_table 
        WHERE accountId = :accountId 
        AND deletedAtMillis IS NULL
    """)
    fun getAccountBalance(accountId: String): Flow<Double?>

    // ----------------------------------------------------
    // DASHBOARD & ANALYTICS (BUSINESS HEALTH)
    // ----------------------------------------------------

    /**
     * 1️⃣ NET PROFIT (Total Kamaya)
     * Ye wo paisa hai jo business ne generate kiya (Sales - Expenses).
     * Owner Withdrawal isay kam nahi karegi.
     */
    @Query("""
        SELECT SUM(profitImpact) 
        FROM transaction_table 
        WHERE deletedAtMillis IS NULL
    """)
    fun getTotalBusinessProfit(): Flow<Double?>

    /**
     * 2️⃣ Floating Profit (Business mein Bacha hua)
     * Formula: Net Profit - Owner Withdrawals
     * Is query se hum sirf Owner Withdrawals ka total nikal rahe hain.
     * Repository mein: NetProfit - Withdrawn = Floating.
     */
    @Query("""
        SELECT SUM(debit) 
        FROM transaction_table 
        WHERE type = 'OWNER_WITHDRAWAL' 
        AND deletedAtMillis IS NULL
    """)
    fun getTotalOwnerWithdrawals(): Flow<Double?>

    /**
     * 3️⃣ Total Outstanding (Market se lena hai)
     * Sirf un logon ka sum jinka balance positive hai.
     * (Note: Ye complex logic hai, SQL mein mushkil hoti hai, behtar hai
     * Repository mein customers list par loop chala kar calculate karen).
     * * Alternative: Total Sales Amount (Aaj ki ya total)
     */
    @Query("""
        SELECT SUM(credit) FROM transaction_table 
        WHERE type = 'SALE' 
        AND deletedAtMillis IS NULL
    """)
    fun getTotalSalesRevenue(): Flow<Double?>

    @Query("""
        SELECT SUM(debit) FROM transaction_table 
        WHERE type = 'EXPENSE' 
        AND deletedAtMillis IS NULL
    """)
    fun getTotalExpenses(): Flow<Double?>

    // ----------------------------------------------------
    // DATE RANGE REPORTS
    // ----------------------------------------------------

    @Query("""
        SELECT * FROM transaction_table 
        WHERE dateMillis BETWEEN :start AND :end 
        AND deletedAtMillis IS NULL 
        ORDER BY dateMillis DESC
    """)
    fun getTransactionsByDateRange(start: Long, end: Long): Flow<List<TransactionEntity>>

    // ----------------------------------------------------
    // SYNC
    // ----------------------------------------------------

    @Query("SELECT * FROM transaction_table WHERE isSynced = 0")
    suspend fun getUnsyncedTransactions(): List<TransactionEntity>

    @Query("UPDATE transaction_table SET isSynced = 1 WHERE transactionId = :id")
    suspend fun markAsSynced(id: String)

    @Query("""
        SELECT IFNULL(SUM(profitImpact), 0)
        FROM transaction_table
        WHERE dateMillis BETWEEN :startMillis AND :endMillis
          AND deletedAtMillis IS NULL
    """)
    fun sumProfitBetween(startMillis: Long, endMillis: Long): Flow<Double>

    @Query("""
        SELECT IFNULL(SUM(profitImpact), 0)
        FROM transaction_table
        WHERE dateMillis = :dateMillis
          AND deletedAtMillis IS NULL
    """)
    fun sumProfitForDate(dateMillis: Long): Flow<Double>

    @Query("""
        SELECT IFNULL(SUM(profitImpact), 0)
        FROM transaction_table
        WHERE deletedAtMillis IS NULL
    """)
    fun sumAllProfit(): Flow<Double>

    @Query("""
        SELECT *
        FROM transaction_table
        WHERE dateMillis BETWEEN :startMillis AND :endMillis
          AND deletedAtMillis IS NULL
        ORDER BY dateMillis ASC
    """)
    fun getBetween(startMillis: Long, endMillis: Long): Flow<List<TransactionEntity>>

    // ------------------------------------------------
    // INSERT
    // ------------------------------------------------
    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insert(transaction: TransactionEntity)

    // ------------------------------------------------
    // ADJUSTMENTS — SALE / PURCHASE
    // ------------------------------------------------

    /** Total adjustment amount for a reference (saleId / purchaseId) */
    @Query("""
        SELECT IFNULL(SUM(credit - debit), 0)
        FROM transaction_table
        WHERE referenceId = :referenceId
          AND type = 'PROFIT_ADJUSTMENT'
          AND deletedAtMillis IS NULL
    """)
    suspend fun getTotalAdjustmentFor(referenceId: String): Double

    /** All adjustments for a specific sale / purchase */
    @Query("""
        SELECT *
        FROM transaction_table
        WHERE referenceId = :referenceId
          AND type = 'PROFIT_ADJUSTMENT'
          AND deletedAtMillis IS NULL
        ORDER BY dateMillis ASC
    """)
    fun getAdjustmentsFor(referenceId: String): Flow<List<TransactionEntity>>

    /** Soft delete an adjustment (undo support) */
    @Query("""
        UPDATE transaction_table
        SET deletedAtMillis = :deletedAtMillis
        WHERE transactionId = :transactionId
    """)
    suspend fun softDeleteAdjustment(
        transactionId: String,
        deletedAtMillis: Long
    )

    @Query(
        """
        SELECT 
        dateMillis, credit, debit, profitImpact, note
        FROM transaction_table
        WHERE accountId = :customerId
        AND deletedAtMillis IS NULL
        ORDER BY dateMillis ASC
    """
    )
    suspend fun customerLedger(customerId: String): List<CustomerLedgerRow>
}
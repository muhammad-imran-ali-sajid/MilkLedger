package com.miassolutions.milkledger.core.localdb.milk

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.miassolutions.milkledger.features.purchase.model.MilkPurchaseUiModel
import com.miassolutions.milkledger.features.purchase.model.PurchaseSummary
import com.miassolutions.milkledger.features.purchase.model.SaleSummary
import com.miassolutions.milkledger.features.sale.model.MilkSaleUiModel
import kotlinx.coroutines.flow.Flow

@Dao
interface MilkDao {

    // 1️⃣ ALL PURCHASES (Weighted Stats)
    @Query("""
SELECT 
    -- 1. Totals
    COALESCE(SUM(mt.totalAmount), 0) AS totalAmount,
    COALESCE(SUM(mt.volume), 0.0) AS totalVolume,

    -- 2. Weighted Average FAT
    COALESCE(
        SUM(CASE WHEN mt.fat > 0 THEN mt.fat * mt.volume ELSE 0 END) /
        NULLIF(SUM(CASE WHEN mt.fat > 0 THEN mt.volume ELSE 0 END), 0),
        0.0
    ) AS avgFat,

    -- 3. Weighted Average LR
    COALESCE(
        SUM(CASE WHEN mt.fat > 0 THEN mt.lr * mt.volume ELSE 0 END) /
        NULLIF(SUM(CASE WHEN mt.fat > 0 THEN mt.volume ELSE 0 END), 0),
        0.0
    ) AS avgLr,

    -- 4. TOTAL TS (sum only)
    COALESCE(SUM(CASE WHEN mt.ts > 0 THEN mt.ts ELSE 0 END), 0.0) AS totalTs,

    -- 5. Quality Coverage Volume
    COALESCE(
        SUM(CASE WHEN mt.fat > 0 THEN mt.volume ELSE 0 END),
        0.0
    ) AS qualityVolume,

    -- 6. Avg Rate (already correct, keep as-is)
    COALESCE(
        SUM(mt.totalAmount) / NULLIF(SUM(mt.volume), 0),
        0.0
    ) AS avgRate,

    -- 7. Total Paid
    (
        SELECT COALESCE(SUM(fl.debit), 0)
        FROM financial_ledger_table fl
        WHERE fl.type = 'CASH_PAID'
          AND fl.dateMillis BETWEEN :start AND :end
          AND fl.deletedAtMillis IS NULL
    ) AS totalPaid

FROM milk_transactions_table mt
WHERE mt.dateMillis BETWEEN :start AND :end
  AND mt.type = 'PURCHASE'
  AND mt.deletedAtMillis IS NULL
""")
    fun getGlobalPurchaseStats(
        start: Long,
        end: Long
    ): Flow<PurchaseSummary>



    // 2️⃣ SUPPLIER SPECIFIC (Weighted Stats)
    @Query("""
    SELECT
        -- 1. Totals
        COALESCE(SUM(mt.totalAmount), 0)      AS totalAmount,
        COALESCE(SUM(mt.volume), 0.0)         AS totalVolume,

        -- 2. Simple Average FAT (sirf jahan quality di gai)
        COALESCE(AVG(CASE WHEN mt.fat > 0 THEN mt.fat END), 0.0) AS avgFat,

        -- 3. Simple Average LR (sirf jahan quality di gai)
        COALESCE(AVG(CASE WHEN mt.fat > 0 THEN mt.lr END), 0.0)  AS avgLr,

        -- 4. TOTAL TS (sum)
        COALESCE(SUM(CASE WHEN mt.ts > 0 THEN mt.ts ELSE 0 END), 0.0) AS totalTs,

        -- 5. Quality Coverage Volume (fat + lr dono diye gaye)
        COALESCE(SUM(CASE WHEN mt.fat > 0 THEN mt.volume ELSE 0 END), 0.0) AS qualityVolume,

        -- 6. Avg Rate (volume weighted – correct)
        COALESCE(SUM(mt.totalAmount) / NULLIF(SUM(mt.volume), 0), 0.0) AS avgRate,

        -- 7. Specific Supplier Paid
        (
            SELECT COALESCE(SUM(fl.debit), 0)
            FROM financial_ledger_table fl
            WHERE fl.accountId = :id
              AND fl.type = 'CASH_PAID'
              AND fl.dateMillis BETWEEN :start AND :end
              AND fl.deletedAtMillis IS NULL
        ) AS totalPaid

    FROM milk_transactions_table mt
    WHERE mt.accountId = :id
      AND mt.dateMillis BETWEEN :start AND :end
      AND mt.type = 'PURCHASE'
      AND mt.deletedAtMillis IS NULL
""")
    fun getSupplierSummary(
        id: String,
        start: Long,
        end: Long
    ): Flow<PurchaseSummary>



    // 3️⃣ ALL SALES (Stats)
// 3️⃣ ALL SALES (Stats)
    @Query("""
        SELECT
            COALESCE(SUM(totalAmount), 0) as totalAmount,
            COALESCE(SUM(volume), 0.0) as grossVolume,
            COALESCE(SUM(deduction), 0.0) as totalDeduction,
            COALESCE(SUM(quantity), 0.0) as netVolume,
            
            -- 🔥 Avg Rate based on PURCHASE Quantity
            -- Formula: (Total Sale Amount) / (Total Purchase Volume in that date range)
            COALESCE(
                SUM(totalAmount) / NULLIF(
                    (
                        SELECT SUM(volume) 
                        FROM milk_transactions_table 
                        WHERE type = 'PURCHASE' 
                          AND dateMillis BETWEEN :start AND :end 
                          AND deletedAtMillis IS NULL
                    ), 0
                ), 
            0.0) as avgRate,

            -- Total Received
            (
                SELECT COALESCE(SUM(credit), 0) 
                FROM financial_ledger_table 
                WHERE type = 'CASH_RECEIVED' 
                  AND dateMillis BETWEEN :start AND :end 
                  AND deletedAtMillis IS NULL
            ) as totalReceived

        FROM milk_transactions_table
        WHERE dateMillis BETWEEN :start AND :end 
          AND type = 'SALE' 
          AND deletedAtMillis IS NULL
    """)
    fun getGlobalSaleStats(start: Long, end: Long): Flow<SaleSummary>


    // 4️⃣ CUSTOMER SPECIFIC (Stats)
    @Query(
        """
    SELECT
        COALESCE(SUM(totalAmount), 0) as totalAmount,
        COALESCE(SUM(volume), 0.0) as grossVolume,
        COALESCE(SUM(deduction), 0.0) as totalDeduction,
        COALESCE(SUM(quantity), 0.0) as netVolume,
        COALESCE(SUM(totalAmount) / NULLIF(SUM(quantity), 0), 0.0) as avgRate,

        -- Specific Customer Received
        (SELECT COALESCE(SUM(credit), 0) FROM financial_ledger_table WHERE accountId = :id AND type='CASH_RECEIVED' AND dateMillis BETWEEN :start AND :end AND deletedAtMillis IS NULL) as totalReceived

    FROM milk_transactions_table
    WHERE accountId = :id AND dateMillis BETWEEN :start AND :end AND type = 'SALE' AND deletedAtMillis IS NULL
"""
    )
    fun getCustomerSummary(id: String, start: Long, end: Long): Flow<SaleSummary>

    // 2️⃣ FOR EDIT SCREEN UI (Pura UI Model fetch karne k liye)
    // Yeh wohi query hai jo List k liye thi, bas WHERE condition change ki hai (ID match)
    @Query(
        """
    SELECT 
        m.milkTransId as id,
        m.dateMillis,
        m.accountId as customerId,
        a.name as customerName,
        
        m.volume as quantity,               -- Maps to 'volume' in UiModel
        m.deduction,
        m.quantity as netQuantity, -- Maps to 'netQuantity' in UiModel
        m.totalAmount,
        m.notes as note,
        m.rateUsed as rate,
        
        -- Payment Detail
        COALESCE(l_pay.credit, 0) as paymentReceived, -- Sale me Paisa ata hy (Credit)
        
        -- 🔥 FIX: Ab hum Ledger ki date nahi, Milk Table ki date utha rahay hain
        m.paymentDateMillis as paymentDateMillis, 
        
        
        -- Running Balance Logic
        (
            SELECT (TOTAL(sub_l.debit) - TOTAL(sub_l.credit))
            FROM financial_ledger_table sub_l
            WHERE sub_l.accountId = m.accountId 
            AND sub_l.deletedAtMillis IS NULL
            AND (
                sub_l.dateMillis < m.dateMillis
                OR
                (sub_l.dateMillis = m.dateMillis)
            )
        ) as currentBalance
        
    FROM milk_transactions_table m
    
    INNER JOIN accounts_table a ON m.accountId = a.accountId
    
    -- Join for Payment (CASH_RECEIVED for Sale)
    LEFT JOIN financial_ledger_table l_pay 
        ON l_pay.referenceId = m.milkTransId 
        AND l_pay.type = 'CASH_RECEIVED' 
        AND l_pay.deletedAtMillis IS NULL

    WHERE m.milkTransId = :id
    LIMIT 1
"""
    )
    suspend fun getSaleDetailById(id: String): MilkSaleUiModel?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(milkTransaction: MilkTransactionEntity): Long

    @Update
    suspend fun update(milkTransaction: MilkTransactionEntity)


    @Query("SELECT * FROM milk_transactions_table WHERE milkTransId = :id")
    suspend fun getMilkTransactionById(id: String): MilkTransactionEntity?


    // Kisi aik Customer ki History dekhne ke liye
    @Query(
        """
        SELECT * FROM milk_transactions_table 
        WHERE accountId = :accountId 
        AND deletedAtMillis IS NULL 
        ORDER BY dateMillis DESC
    """
    )
    fun getMilkHistoryByAccount(accountId: String): Flow<List<MilkTransactionEntity>>

    // Soft Delete
    @Query("UPDATE milk_transactions_table SET deletedAtMillis = :time WHERE milkTransId = :id")
    suspend fun softDelete(id: String, time: Long)

    @Query("UPDATE milk_transactions_table SET deletedAtMillis = :deleteTime WHERE milkTransId = :id")
    suspend fun softDeleteMilkTransaction(id: String, deleteTime: Long)


    @Query(
        """
    SELECT 
        m.milkTransId as id,
        m.dateMillis,
        m.accountId as customerId,
        a.name as customerName,
        m.volume as quantity, 
        m.deduction as deduction,
        m.quantity as netQuantity,
        m.totalAmount,
        m.notes as note,
        m.rateUsed as rate,
          
        COALESCE(l_pay.credit, 0) as paymentReceived, 
        
        m.paymentDateMillis as paymentDateMillis,
        
        -- 🔥 ACCUMULATED BALANCE (Same logic)
        (
            SELECT (TOTAL(sub_l.debit) - TOTAL(sub_l.credit))
            FROM financial_ledger_table sub_l
            WHERE sub_l.accountId = m.accountId 
            AND sub_l.deletedAtMillis IS NULL
            AND (
                sub_l.dateMillis < m.dateMillis
                OR
                (sub_l.dateMillis = m.dateMillis)
            )
        ) as currentBalance
        
    FROM milk_transactions_table m
    
    INNER JOIN accounts_table a ON m.accountId = a.accountId
    
    -- Payment Join (Jo pehle se tha)
    LEFT JOIN financial_ledger_table l_pay 
        ON l_pay.referenceId = m.milkTransId 
        AND l_pay.type = 'CASH_RECEIVED' 
        AND l_pay.deletedAtMillis IS NULL

    WHERE m.dateMillis =:dateMillis
    AND m.deletedAtMillis IS NULL
    AND m.type = 'SALE'
    AND m.deletedAtMillis IS NULL
    
    ORDER BY sortOrder ASC
"""
    )
    fun getMilkSalesByDate(dateMillis: Long): Flow<List<MilkSaleUiModel>>


    @Query(
        """
        SELECT 
            m.milkTransId as id,
            m.dateMillis,
            m.accountId as customerId,
            a.name as customerName,
            m.volume as quantity, 
            m.deduction as deduction,
            m.quantity as netQuantity,
            m.totalAmount,
            m.notes as note,
            m.rateUsed as rate,

            -- 🔥 NEW: Previous Rate Logic (Subquery for Sale)
            -- Ye check karega ke is customer ka pichla rate kya tha
            (
                SELECT prev.rateUsed 
                FROM milk_transactions_table prev 
                WHERE prev.accountId = m.accountId 
                AND prev.type = 'SALE' -- ✅ Ensure SALE type
                AND prev.deletedAtMillis IS NULL
                AND prev.dateMillis < m.dateMillis -- Is date se purana
                ORDER BY prev.dateMillis DESC -- Sab se qareebi purana
                LIMIT 1
            ) as previousRate,
            
            -- Payment Data
            COALESCE(l_pay.credit, 0) as paymentReceived, 
            m.paymentDateMillis as paymentDateMillis,
            
            -- Running Balance Calculation
            (
            SELECT (TOTAL(sub_l.debit) - TOTAL(sub_l.credit))
            FROM financial_ledger_table sub_l
            WHERE sub_l.accountId = m.accountId 
            AND sub_l.deletedAtMillis IS NULL
            AND (
                sub_l.dateMillis < m.dateMillis
                OR (sub_l.dateMillis = m.dateMillis)
            )
        ) as currentBalance
            
        FROM milk_transactions_table m
        
        INNER JOIN accounts_table a ON m.accountId = a.accountId
        
        LEFT JOIN financial_ledger_table l_pay 
            ON l_pay.referenceId = m.milkTransId 
            AND l_pay.type = 'CASH_RECEIVED' 
            AND l_pay.deletedAtMillis IS NULL

        WHERE m.accountId = :accountId 
        AND m.deletedAtMillis IS NULL
        AND m.dateMillis BETWEEN :startDate AND :endDate
        
        
        ORDER BY m.dateMillis ASC, m.createdAtMillis ASC
    """
    )
    fun getCustomerSalesHistory(
        accountId: String,
        startDate: Long,
        endDate: Long
    ): Flow<List<MilkSaleUiModel>>


    /************************** Purchase ******************************/


    // 🔥 PURCHASE DETAIL QUERY
    @Query(
        """
    SELECT 
        m.milkTransId as id,
        m.dateMillis,
        m.accountId as supplierId,
        a.name as supplierName,
        
        -- Measurements
        m.volume,
        COALESCE(m.fat, 0.0) as fat,
        COALESCE(m.lr, 0.0) as lr,
        COALESCE(m.ts, 0.0) as ts,
        
        m.rateUsed as rate,
        m.totalAmount,
        m.notes as note,
        
        -- Payment Detail
        COALESCE(l_pay.debit, 0) as paymentMade,
        
        -- 🔥 FIX: Ledger ki date (l_pay) nahi, Milk Table ki date (m) uthayen
        m.paymentDateMillis as paymentDateMillis, 
        
        -- Running Balance
        (
            SELECT (TOTAL(sub_l.debit) - TOTAL(sub_l.credit))
            FROM financial_ledger_table sub_l
            WHERE sub_l.accountId = m.accountId 
            AND sub_l.deletedAtMillis IS NULL
            AND (
                sub_l.dateMillis < m.dateMillis
                OR (sub_l.dateMillis = m.dateMillis)
            )
        ) as currentBalance
        
    FROM milk_transactions_table m
    INNER JOIN accounts_table a ON m.accountId = a.accountId
    
    -- Join for Payment (CASH_PAID)
    LEFT JOIN financial_ledger_table l_pay 
        ON l_pay.referenceId = m.milkTransId 
        AND l_pay.type = 'CASH_PAID' 
        AND l_pay.deletedAtMillis IS NULL

    WHERE m.milkTransId = :id
    LIMIT 1
"""
    )
    suspend fun getPurchaseDetailById(id: String): MilkPurchaseUiModel?

    // 🔥 PURCHASE LIST BY DATE
    @Query(
        """
    SELECT 
        m.milkTransId as id,
        m.dateMillis,
        m.accountId as supplierId,
        a.name as supplierName,
        
        m.volume,
        COALESCE(m.fat, 0.0) as fat,
        COALESCE(m.lr, 0.0) as lr,
        COALESCE(m.ts, 0.0) as ts,
        
        m.rateUsed as rate,
        m.totalAmount,
        m.notes as note,
        
        -- Fix: Explicitly naming the alias to match Data Class field
        COALESCE(l_pay.debit, 0) as paymentMade,
        m.paymentDateMillis as paymentDateMillis, 
        
        
        -- Running Balance Logic
        (
            SELECT (TOTAL(sub_l.debit) - TOTAL(sub_l.credit))
            FROM financial_ledger_table sub_l
            WHERE sub_l.accountId = m.accountId 
            AND sub_l.deletedAtMillis IS NULL
            AND (
                sub_l.dateMillis < m.dateMillis
                OR (sub_l.dateMillis = m.dateMillis)
            )
        ) as currentBalance
        
    FROM milk_transactions_table m
    INNER JOIN accounts_table a ON m.accountId = a.accountId
    
    -- Yahan l_pay.dateMillis ko ensure karein ke reference sahi hai
    LEFT JOIN financial_ledger_table l_pay 
        ON l_pay.referenceId = m.milkTransId 
        AND l_pay.type = 'CASH_PAID' 
        AND l_pay.deletedAtMillis IS NULL

    WHERE m.type = 'PURCHASE' 
    AND m.dateMillis = :dateMillis
    AND m.deletedAtMillis IS NULL
    
    ORDER BY sortOrder ASC
"""
    )
    fun getPurchasesByDate(dateMillis: Long): Flow<List<MilkPurchaseUiModel>>


    @Query(
        """
        SELECT 
            m.milkTransId as id,
            m.dateMillis,
            m.accountId as supplierId,
            a.name as supplierName,
            
            -- Measurements
            m.volume,
            COALESCE(m.fat, 0.0) as fat,
            COALESCE(m.lr, 0.0) as lr,
            COALESCE(m.ts, 0.0) as ts,
            
            m.rateUsed as rate,
            
            -- 🔥 NEW: Previous Rate Logic (Subquery)
            -- Ye query check karegi ke is date se pehle, isi supplier ka last rate kya tha
            (
                SELECT prev.rateUsed 
                FROM milk_transactions_table prev 
                WHERE prev.accountId = m.accountId 
                AND prev.type = 'PURCHASE' 
                AND prev.deletedAtMillis IS NULL
                AND prev.dateMillis < m.dateMillis -- Is date se purana
                ORDER BY prev.dateMillis DESC -- Sab se qareebi purana
                LIMIT 1
            ) as previousRate,
            
            m.totalAmount,
            m.notes as note,
            
            -- Payment (Purchase me Payment = Debit)
            COALESCE(l_pay.debit, 0) as paymentMade,
            m.paymentDateMillis as paymentDateMillis, 
            
            -- Running Balance Calculation (Specific for Supplier)
            (
            SELECT (TOTAL(sub_l.debit) - TOTAL(sub_l.credit))
            FROM financial_ledger_table sub_l
            WHERE sub_l.accountId = m.accountId 
            AND sub_l.deletedAtMillis IS NULL
            AND (
                sub_l.dateMillis < m.dateMillis
                OR (sub_l.dateMillis = m.dateMillis)
            )
        ) as currentBalance
            
        FROM milk_transactions_table m
        INNER JOIN accounts_table a ON m.accountId = a.accountId
        
        LEFT JOIN financial_ledger_table l_pay 
            ON l_pay.referenceId = m.milkTransId 
            AND l_pay.type = 'CASH_PAID' 
            AND l_pay.deletedAtMillis IS NULL

        WHERE m.accountId = :supplierId
        AND m.type = 'PURCHASE' 
        AND m.deletedAtMillis IS NULL
        AND m.dateMillis BETWEEN :startDate AND :endDate
        
        ORDER BY m.dateMillis ASC, m.createdAtMillis ASC
    """
    )
    fun getSupplierHistory(
        supplierId: String,
        startDate: Long,
        endDate: Long
    ): Flow<List<MilkPurchaseUiModel>>


    // Aaj ki date me kin suppliers se purchase hui?
    @Query("SELECT DISTINCT accountId FROM milk_transactions_table WHERE dateMillis = :dateMillis AND type = 'PURCHASE' AND deletedAtMillis IS NULL")
    fun getSuppliersWithPurchaseOnDate(dateMillis: Long): Flow<List<String>>
}















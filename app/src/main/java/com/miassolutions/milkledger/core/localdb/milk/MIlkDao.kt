package com.miassolutions.milkledger.core.localdb.milk

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.miassolutions.milkledger.features.purchase.model.MilkPurchaseUiModel
import com.miassolutions.milkledger.features.sale.model.MilkSaleUiModel
import kotlinx.coroutines.flow.Flow

@Dao
interface MilkDao {

    // 2️⃣ FOR EDIT SCREEN UI (Pura UI Model fetch karne k liye)
    // Yeh wohi query hai jo List k liye thi, bas WHERE condition change ki hai (ID match)
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
            
            -- 🔥 YE LINE MISSING THI 👇
            m.rateUsed as rate,
            
            COALESCE(l_pay.credit, 0) as paymentReceived, 
            
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


    @Query("""
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
        
        -- 🔥 NEW ADDITION: Payment ki Date uthao
        l_pay.dateMillis as paymentDateMillis,
        
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

    WHERE m.dateMillis BETWEEN :start AND :end 
    AND m.deletedAtMillis IS NULL
    
    ORDER BY m.dateMillis DESC, m.createdAtMillis DESC
""")
    fun getMilkSalesByDate(start: Long, end: Long): Flow<List<MilkSaleUiModel>>



    @Query("""
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
            
            -- Payment Data
            COALESCE(l_pay.credit, 0) as paymentReceived, 
            l_pay.dateMillis as paymentDateMillis,
            
            -- 🔥 RUNNING BALANCE CALCULATION FOR SPECIFIC CUSTOMER 🔥
            (
                SELECT (TOTAL(sub_l.debit) - TOTAL(sub_l.credit))
                FROM financial_ledger_table sub_l
                WHERE sub_l.accountId = :accountId  -- ✅ Sirf is customer ka hisaab
                AND sub_l.deletedAtMillis IS NULL
                AND (
                    sub_l.dateMillis < m.dateMillis
                    OR
                    (sub_l.dateMillis = m.dateMillis)
                )
            ) as currentBalance
            
        FROM milk_transactions_table m
        
        INNER JOIN accounts_table a ON m.accountId = a.accountId
        
        LEFT JOIN financial_ledger_table l_pay 
            ON l_pay.referenceId = m.milkTransId 
            AND l_pay.type = 'CASH_RECEIVED' 
            AND l_pay.deletedAtMillis IS NULL

        -- 🔥 FILTER LOGIC HERE
        WHERE m.accountId = :accountId 
        AND m.deletedAtMillis IS NULL
        AND m.dateMillis BETWEEN :startDate AND :endDate
        
        ORDER BY m.dateMillis ASC, m.createdAtMillis ASC
    """)
    fun getCustomerSalesHistory(accountId: String, startDate: Long, endDate: Long): Flow<List<MilkSaleUiModel>>


    /************************** Purchase ******************************/


    // 🔥 PURCHASE DETAIL QUERY
    @Query("""
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
            COALESCE(l_pay.debit, 0) as paymentMade, -- 🔥 Note: Payment is Debit in Purchase
            l_pay.dateMillis as paymentDateMillis,
            
            -- Running Balance (Supplier ka hisaab)
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
    """)
    suspend fun getPurchaseDetailById(id: String): MilkPurchaseUiModel?
}















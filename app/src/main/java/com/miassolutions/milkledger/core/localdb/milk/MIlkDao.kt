package com.miassolutions.milkledger.core.localdb.milk

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.miassolutions.milkledger.features.milk.SaleDetailTuple
import com.miassolutions.milkledger.features.sale.domain.model.MilkSaleUiModel
import kotlinx.coroutines.flow.Flow

@Dao
interface MilkDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(milkTransaction: MilkTransactionEntity): Long

    @Update
    suspend fun update(milkTransaction: MilkTransactionEntity)


    @Transaction // Safe side k liye
    @Query(
        """
        SELECT 
            m.*, 
            
            -- Account Columns (Prefix k sath map kar rahe hain)
            a.accountId as acc_accountId,
            a.name as acc_name,
            a.phone as acc_phone,
            a.accountType as acc_accountType,
            a.sortOrder as acc_sortOrder,
            a.advanceAmount as acc_advanceAmount,
            a.defaultRate as acc_defaultRate,
            a.initialBalance as acc_initialBalance,
            a.createdAtMillis as acc_createdAtMillis,
            a.updatedAtMillis as acc_updatedAtMillis,
            a.isSynced as acc_isSynced,
            a.deletedAtMillis as acc_deletedAtMillis,

            -- Payment Info (Ledger se)
            l.credit as paymentAmount,
            l.dateMillis as paymentDate

        FROM milk_transactions_table m
        
        -- 1. Join Customer
        INNER JOIN accounts_table a ON m.accountId = a.accountId
        
        -- 2. Join Payment (Sirf wo entry jo CASH_RECEIVED ho aur isi sale se linked ho)
        LEFT JOIN financial_ledger_table l 
            ON l.referenceId = m.milkTransId 
            AND l.type = 'CASH_RECEIVED' 
            AND l.deletedAtMillis IS NULL

        WHERE m.milkTransId = :saleId
    """
    )
    suspend fun getSaleDetailById(saleId: String): SaleDetailTuple?


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
        
        COALESCE(l_pay.credit, 0) as paymentReceived, 
        
        -- 🔥 FIXED: Accurate Running Balance
        (
            SELECT (TOTAL(sub_l.debit) - TOTAL(sub_l.credit))
            FROM financial_ledger_table sub_l
            WHERE sub_l.accountId = m.accountId 
            AND sub_l.deletedAtMillis IS NULL
            
            AND (
                -- 1. Pichli Dates ka sara hisaab (Purani history)
                sub_l.dateMillis < m.dateMillis
                
                OR 
                
                -- 2. Aaj ke din ki wo entries jo is Sale se PEHLE create huin
                -- (Note: Hum < use kar rahy hen, <= nahi, taake duplication na ho)
                (sub_l.dateMillis = m.dateMillis AND sub_l.createdAtMillis < l_main.createdAtMillis)
                
                OR
                
                -- 3. 🔥 MAGIC FIX: Is Current Transaction ki saari entries (Sale + Payment)
                -- Chahe Payment 1ms baad hi kyu na bani ho, agar ID same hai to shamil karo!
                sub_l.referenceId = m.milkTransId
            )
        ) as currentBalance
        
    FROM milk_transactions_table m
    
    INNER JOIN accounts_table a ON m.accountId = a.accountId
    
    -- Main Ledger (Time reference k liye)
    LEFT JOIN financial_ledger_table l_main 
        ON l_main.referenceId = m.milkTransId 
        AND l_main.type = 'MILK_SALE' 
        AND l_main.deletedAtMillis IS NULL

    -- Payment Ledger (Amount k liye)
    LEFT JOIN financial_ledger_table l_pay 
        ON l_pay.referenceId = m.milkTransId 
        AND l_pay.type = 'CASH_RECEIVED' 
        AND l_pay.deletedAtMillis IS NULL

    WHERE m.dateMillis BETWEEN :start AND :end 
    AND m.deletedAtMillis IS NULL
    
    ORDER BY m.dateMillis DESC, m.createdAtMillis DESC
""")
    fun getMilkSalesByDate(start: Long, end: Long): Flow<List<MilkSaleUiModel>>
}
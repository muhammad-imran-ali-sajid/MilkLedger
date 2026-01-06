package com.miassolutions.milkledger.core.localdb.milk

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.miassolutions.milkledger.features.sale.domain.model.MilkSaleUiModel
import kotlinx.coroutines.flow.Flow

@Dao
interface MilkDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(milkTransaction: MilkTransactionEntity): Long

    @Update
    suspend fun update(milkTransaction: MilkTransactionEntity)

    // Kisi aik Customer ki History dekhne ke liye
    @Query("""
        SELECT * FROM milk_transactions_table 
        WHERE accountId = :accountId 
        AND deletedAtMillis IS NULL 
        ORDER BY dateMillis DESC
    """)
    fun getMilkHistoryByAccount(accountId: String): Flow<List<MilkTransactionEntity>>

    // Soft Delete
    @Query("UPDATE milk_transactions_table SET deletedAtMillis = :time WHERE milkTransId = :id")
    suspend fun softDelete(id: String, time: Long)

    @Query("""
    SELECT 
        m.milkTransId as id,
        m.dateMillis,
        m.accountId as customerId,  -- ✅ Fix 1: Alias match karwaya (accountId -> customerId)
        a.name as customerName,
        m.quantity, 
        0.0 as deduction,
        m.quantity as netQuantity,
        m.totalAmount,
        m.notes as note,
        
        -- ✅ Fix 2: Dummy values pass karein taake Crash na ho
        0 as paymentReceived, 
        0 as currentBalance
        
    FROM milk_transactions_table m
    INNER JOIN accounts_table a ON m.accountId = a.accountId
    WHERE m.dateMillis BETWEEN :start AND :end 
    AND m.deletedAtMillis IS NULL
    ORDER BY m.createdAtMillis DESC
""")
    fun getMilkSalesByDate(start: Long, end: Long): Flow<List<MilkSaleUiModel>>
}
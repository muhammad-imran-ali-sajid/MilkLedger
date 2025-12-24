package com.miassolutions.milkledger.data.local.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.miassolutions.milkledger.data.local.entities.ProfitReceiptEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfitReceiptDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(receipt: ProfitReceiptEntity)

    @Query("""
        SELECT IFNULL(SUM(amountReceived), 0)
        FROM profit_receipt_table
        WHERE deletedAtMillis IS NULL
    """)
    fun getTotalReceivedProfit(): Flow<Double>

    @Query("""
        SELECT * FROM profit_receipt_table
        WHERE dateMillis BETWEEN :startMillis AND :endMillis
          AND deletedAtMillis IS NULL
        ORDER BY dateMillis DESC
    """)
    fun getBetween(
        startMillis: Long,
        endMillis: Long
    ): Flow<List<ProfitReceiptEntity>>
}

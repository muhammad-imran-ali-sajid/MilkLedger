package com.miassolutions.milkledger.data.local.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.miassolutions.milkledger.data.local.entities.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    // ------------------------------------------------
    // INSERT
    // ------------------------------------------------
    @Insert(onConflict = OnConflictStrategy.REPLACE)
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
}


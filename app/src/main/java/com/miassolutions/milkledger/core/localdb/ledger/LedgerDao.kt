package com.miassolutions.milkledger.core.localdb.ledger

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface LedgerDao {

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
}
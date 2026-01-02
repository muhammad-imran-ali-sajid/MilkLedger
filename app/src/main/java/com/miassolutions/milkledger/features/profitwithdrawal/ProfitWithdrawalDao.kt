package com.miassolutions.milkledger.features.profitwithdrawal

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfitWithdrawalDao {

    // 1️⃣ Insert (Naya withdrawal record karna)
    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insert(withdrawal: ProfitWithdrawalEntity)

    // 2️⃣ Update (Agar amount ya note edit karna ho)
    @Update
    suspend fun update(withdrawal: ProfitWithdrawalEntity)

    // 3️⃣ Soft Delete (Record ko delete mark karna)
    @Query("""
        UPDATE profit_withdrawal_table 
        SET deletedAtMillis = :deletedAt, isSynced = 0 
        WHERE drawingId = :id
    """)
    suspend fun softDelete(id: String, deletedAt: Long)

    // ----------------------------------------------------
    // READ QUERIES (UI & Dashboard)
    // ----------------------------------------------------

    // 4️⃣ Get All (History list dikhane ke liye - Newest First)
    @Query("""
        SELECT * FROM profit_withdrawal_table 
        WHERE deletedAtMillis IS NULL 
        ORDER BY dateMillis DESC
    """)
    fun getAllWithdrawals(): Flow<List<ProfitWithdrawalEntity>>

    // 5️⃣ Get Total Withdrawn (Dashboard: "Wasool Kiya" Calculation)
    // Ye query sab se important hai. Ye batayegi ke owner ab tak kitna paisa nikal chuka hai.
    @Query("""
        SELECT SUM(amount) FROM profit_withdrawal_table 
        WHERE deletedAtMillis IS NULL
    """)
    fun getTotalWithdrawnAmount(): Flow<Double?>

    // 6️⃣ Specific Date Range (Report ke liye)
    @Query("""
        SELECT * FROM profit_withdrawal_table 
        WHERE dateMillis BETWEEN :startMillis AND :endMillis 
        AND deletedAtMillis IS NULL 
        ORDER BY dateMillis DESC
    """)
    fun getWithdrawalsByDateRange(startMillis: Long, endMillis: Long): Flow<List<ProfitWithdrawalEntity>>

    // 7️⃣ Get Single (Edit screen open karne ke liye)
    @Query("SELECT * FROM profit_withdrawal_table WHERE drawingId = :id")
    suspend fun getById(id: String): ProfitWithdrawalEntity?

    // ----------------------------------------------------
    // SYNC QUERIES (Remote Backup)
    // ----------------------------------------------------

    @Query("SELECT * FROM profit_withdrawal_table WHERE isSynced = 0")
    suspend fun getUnsyncedWithdrawals(): List<ProfitWithdrawalEntity>

    @Query("UPDATE profit_withdrawal_table SET isSynced = 1 WHERE drawingId = :id")
    suspend fun markAsSynced(id: String)
}
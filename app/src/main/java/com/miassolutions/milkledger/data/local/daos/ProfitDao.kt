package com.miassolutions.milkledger.data.local.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.miassolutions.milkledger.data.local.entities.ProfitEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface ProfitDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(profitList: List<ProfitEntity>)

    @Query("DELETE FROM profit_table")
    suspend fun clearAll()

    @Query("DELETE FROM profit_table WHERE profitId = :profitId")
    suspend fun deleteProfit(profitId: String)

    @Upsert
    suspend fun upsert(profit: ProfitEntity)

    @Query("SELECT * FROM profit_table ORDER BY receivedDate DESC")
    fun getAllProfitFlow(): Flow<List<ProfitEntity>>

    @Query("SELECT * FROM profit_table WHERE profitId = :profitId LIMIT 1")
    suspend fun getProfitById(profitId: String): ProfitEntity?

    // -------------------------------
    // DAILY
    // -------------------------------
    @Query("SELECT * FROM profit_table WHERE receivedDate = :date")
    fun getDailyReceivedProfit(date: LocalDate): Flow<List<ProfitEntity>>

    @Query("SELECT * FROM profit_table WHERE receivedDate = :date")
    fun getDailyFlow(date: LocalDate): Flow<List<ProfitEntity>>

    // -------------------------------
    // RANGE (WEEKLY / CUSTOM)
    // -------------------------------
    @Query("SELECT * FROM profit_table WHERE receivedDate BETWEEN :start AND :end")
    suspend fun getBetween(start: LocalDate, end: LocalDate): List<ProfitEntity>

    @Query("SELECT * FROM profit_table WHERE receivedDate BETWEEN :start AND :end")
    fun getReceivedProfitBetweenFlow(start: LocalDate, end: LocalDate): Flow<List<ProfitEntity>>

    // -------------------------------
    // MONTHLY (yyyy-MM)
    // -------------------------------
    @Query(
        """
        SELECT * FROM profit_table 
        WHERE receivedDate LIKE :yearMonth || '%'
        ORDER BY receivedDate ASC
    """
    )
    fun getReceivedProfitMonthly(yearMonth: String): Flow<List<ProfitEntity>>
    // Input example → "2025-01"

    // -------------------------------
    // YEARLY (yyyy)
    // -------------------------------
    @Query(
        """
        SELECT * FROM profit_table 
        WHERE receivedDate LIKE :year || '%'
        ORDER BY receivedDate ASC
    """
    )
    fun getReceivedProfitYearly(year: String): Flow<List<ProfitEntity>>
    // Input example → "2025"

    @Query("SELECT * FROM profit_table")
    suspend fun getAll(): List<ProfitEntity>

    @Query("SELECT * FROM profit_table")
     fun getAllFlow(): Flow<List<ProfitEntity>>
}

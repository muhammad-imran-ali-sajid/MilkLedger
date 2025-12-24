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

    /* ---------------------------
       Sync / Raw
    --------------------------- */

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(profits: List<ProfitEntity>)

    @Upsert
    suspend fun upsert(profit: ProfitEntity)

    @Query("DELETE FROM profit_table")
    suspend fun clearAll()

    @Query("""
        UPDATE profit_table
        SET deletedAtMillis = :deletedAtMillis
        WHERE profitId = :profitId
    """)
    suspend fun softDeleteById(profitId: String, deletedAtMillis: Long)

    /* ---------------------------
       Base Queries
    --------------------------- */

    @Query("""
        SELECT * FROM profit_table
        WHERE deletedAtMillis IS NULL
        ORDER BY dateMillis DESC
    """)
    fun getAllProfitFlow(): Flow<List<ProfitEntity>>

    @Query("""
        SELECT * FROM profit_table
        WHERE profitId = :profitId
          AND deletedAtMillis IS NULL
        LIMIT 1
    """)
    suspend fun getProfitById(profitId: String): ProfitEntity?

    /* ---------------------------
       Daily
    --------------------------- */

    @Query("""
        SELECT * FROM profit_table
        WHERE dateMillis = :dateMillis
          AND deletedAtMillis IS NULL
    """)
    fun getDailyProfit(dateMillis: Long): Flow<List<ProfitEntity>>

    /* ---------------------------
       Range (Weekly / Custom)
    --------------------------- */

    @Query("""
        SELECT * FROM profit_table
        WHERE dateMillis BETWEEN :startMillis AND :endMillis
          AND deletedAtMillis IS NULL
        ORDER BY dateMillis ASC
    """)
    suspend fun getBetween(
        startMillis: Long,
        endMillis: Long
    ): List<ProfitEntity>

    @Query("""
        SELECT * FROM profit_table
        WHERE dateMillis BETWEEN :startMillis AND :endMillis
          AND deletedAtMillis IS NULL
        ORDER BY dateMillis ASC
    """)
    fun getBetweenFlow(
        startMillis: Long,
        endMillis: Long
    ): Flow<List<ProfitEntity>>

    /* ---------------------------
       Monthly / Yearly
       (computed via millis ranges)
    --------------------------- */

    @Query("""
        SELECT * FROM profit_table
        WHERE dateMillis BETWEEN :startMillis AND :endMillis
          AND deletedAtMillis IS NULL
        ORDER BY dateMillis ASC
    """)
    fun getMonthlyProfit(
        startMillis: Long,
        endMillis: Long
    ): Flow<List<ProfitEntity>>
}


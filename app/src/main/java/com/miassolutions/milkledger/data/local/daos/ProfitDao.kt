package com.miassolutions.milkledger.data.local.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.miassolutions.milkledger.data.local.entities.ProfitEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfitDao {


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(profitList: List<ProfitEntity>)


    @Upsert
    suspend fun upsert(profit: ProfitEntity)

    @Query("SELECT * FROM profit_table ORDER BY receivedDate DESC")
    fun getAllProfitFlow(): Flow<List<ProfitEntity>>

    @Query("SELECT * FROM profit_table WHERE profitId = :profitId LIMIT 1")
    suspend fun getProfitById(profitId: String): ProfitEntity?
}
package com.miassolutions.milkledger.data.repository

import com.miassolutions.milkledger.data.local.daos.ProfitDao
import com.miassolutions.milkledger.data.local.entities.ProfitEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ProfitRepository @Inject constructor(
    private val dao: ProfitDao
) {

    suspend fun upsert(profit: ProfitEntity) = dao.upsert(profit)

    suspend fun delete(profit: ProfitEntity) = dao.deleteProfit(profit)

    suspend fun getProfitById(id: String): ProfitEntity? = dao.getProfitById(id)

    fun getAllProfitList(): Flow<List<ProfitEntity>> = dao.getAllProfitFlow()

    suspend fun upsertAll(profitList: List<ProfitEntity>) = dao.upsertAll(profitList)

}
package com.miassolutions.milkledger.data.repository

import com.miassolutions.milkledger.data.local.daos.ProfitDao
import com.miassolutions.milkledger.data.local.entities.ProfitEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate
import javax.inject.Inject

class ProfitRepository @Inject constructor(
    private val dao: ProfitDao,
    private val mDao : AnalyticsRepository

) {

    suspend fun upsert(profit: ProfitEntity) = dao.upsert(profit)

    suspend fun delete(profit: ProfitEntity) = dao.deleteProfit(profit)

    fun getNetProfit() = mDao.getProfitAll()

    fun getProfitBetween(start: LocalDate, end: LocalDate): Flow<Double> =
        combine(
            mDao.getTotalSalesBetween(start, end),
            mDao.getTotalPurchasesBetween(start, end),
            mDao.getTotalExpensesBetween(start, end)
        ) { sales, purchases, expenses ->
            val totalSales = sales ?: 0.0
            val totalPurchases = purchases ?: 0.0
            val totalExpenses = expenses ?: 0.0
            totalSales - (totalPurchases + totalExpenses)
        }

    suspend fun getProfitById(id: String): ProfitEntity? = dao.getProfitById(id)

    fun getAllProfitList(): Flow<List<ProfitEntity>> = dao.getAllProfitFlow()

    suspend fun upsertAll(profitList: List<ProfitEntity>) = dao.upsertAll(profitList)

}
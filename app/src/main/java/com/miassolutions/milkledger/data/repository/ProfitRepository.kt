package com.miassolutions.milkledger.data.repository

import com.miassolutions.milkledger.data.local.daos.ProfitDao
import com.miassolutions.milkledger.data.local.entities.ProfitEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import java.time.LocalDate
import javax.inject.Inject

class ProfitRepository @Inject constructor(
    private val dao: ProfitDao,
    private val mDao: AnalyticsRepository
) {

    // --------------------------------------------------------------------
    // CRUD
    // --------------------------------------------------------------------
    suspend fun upsert(profit: ProfitEntity) = dao.upsert(profit)

    suspend fun delete(profit: ProfitEntity) = dao.deleteProfit(profit)

    suspend fun upsertAll(profitList: List<ProfitEntity>) = dao.upsertAll(profitList)

    suspend fun getProfitById(id: String): ProfitEntity? = dao.getProfitById(id)

    fun getAllProfitList(): Flow<List<ProfitEntity>> = dao.getAllProfitFlow()

    suspend fun getAll(): List<ProfitEntity> = dao.getAll()


    // --------------------------------------------------------------------
    // LIST FILTERS
    // --------------------------------------------------------------------

    suspend fun getDaily(date: LocalDate): List<ProfitEntity> =
        dao.getDaily(date)

    suspend fun getWeekly(start: LocalDate, end: LocalDate): List<ProfitEntity> =
        dao.getBetween(start, end) // FIXED bug (you used start, start)

    suspend fun getMonthly(date: LocalDate): List<ProfitEntity> {
        val ym = "${date.year}-${"%02d".format(date.monthValue)}"
        return dao.getMonthly(ym)
    }

    suspend fun getYearly(date: LocalDate): List<ProfitEntity> =
        dao.getYearly(date.year.toString())

    suspend fun getCustom(start: LocalDate, end: LocalDate): List<ProfitEntity> =
        dao.getBetween(start, end)


    // --------------------------------------------------------------------
    // NET PROFIT CALCULATIONS (Sales - Purchases - Expenses)
    // --------------------------------------------------------------------

    fun getNetProfit(): Flow<Double> = mDao.getProfitAll()

    // Daily
    fun getNetProfitDaily(date: LocalDate): Flow<Double> =
        combine(
            mDao.getTotalSalesDaily(date),
            mDao.getTotalPurchasesDaily(date),
            mDao.getTotalExpensesDaily(date),
        ) { s, p, e ->

            val totalSales = s ?: 0.0
            val totalPurchases = p ?: 0.0
            val totalExpenses = e ?: 0.0

            totalSales - (totalPurchases + totalExpenses)
        }

    // Weekly / Custom
    suspend fun getNetProfitWeekly(start: LocalDate, end: LocalDate): Double {
        val sales = mDao.getTotalSalesBetween(start, end).firstOrZero()
        val purchases = mDao.getTotalPurchasesBetween(start, end).firstOrZero()
        val expenses = mDao.getTotalExpensesBetween(start, end).firstOrZero()
        return sales - (purchases + expenses)
    }

    suspend fun getNetProfitCustom(start: LocalDate, end: LocalDate): Double =
        getNetProfitWeekly(start, end) // Same logic

    // Monthly
    suspend fun getNetProfitMonthly(year: Int, month: Int): Double {
        val start = LocalDate.of(year, month, 1)
        val end = start.withDayOfMonth(start.lengthOfMonth())
        return getNetProfitWeekly(start, end)
    }

    // Yearly
    suspend fun getNetProfitYearly(year: Int): Double {
        val start = LocalDate.of(year, 1, 1)
        val end = LocalDate.of(year, 12, 31)
        return getNetProfitWeekly(start, end)
    }


    // --------------------------------------------------------------------
    // Helpers
    // --------------------------------------------------------------------

    private suspend fun Flow<Double?>.firstOrZero(): Double {
        return this.firstOrNull() ?: 0.0
    }
}

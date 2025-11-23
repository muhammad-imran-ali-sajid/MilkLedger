package com.miassolutions.milkledger.data.repository

import com.miassolutions.milkledger.data.local.daos.ReportsDao
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate

@Singleton
class AnalyticsRepository @Inject constructor(
    private val reportsDao: ReportsDao
) {

    // ───────────────────────────────
    // 📅 RANGE-BASED QUERIES
    // ───────────────────────────────

    fun getTotalMilkPurchaseBetween(start: LocalDate, end: LocalDate): Flow<Double?> =
        reportsDao.getTotalMilkPurchaseBetween(start, end)

    fun getTotalMilkSoldBetween(start: LocalDate, end: LocalDate): Flow<Double?> =
        reportsDao.getTotalMilkSoldBetween(start, end)

    fun getAvgFatBetween(start: LocalDate, end: LocalDate): Flow<Double?> =
        reportsDao.getAvgFatBetween(start, end)

    fun getAvgLrBetween(start: LocalDate, end: LocalDate): Flow<Double?> =
        reportsDao.getAvgLrBetween(start, end)

    fun getTotalTsBetween(start: LocalDate, end: LocalDate): Flow<Double?> =
        reportsDao.getTsBetween(start, end)

    fun getTotalSalesBetween(start: LocalDate, end: LocalDate): Flow<Double?> =
        reportsDao.getTotalSalesBetween(start, end)

    fun getTotalPurchasesBetween(start: LocalDate, end: LocalDate): Flow<Double?> =
        reportsDao.getTotalPurchasesBetween(start, end)

    fun getTotalExpensesBetween(start: LocalDate, end: LocalDate): Flow<Double?> =
        reportsDao.getTotalExpensesBetween(start, end)

    fun getTotalFixedExpensesBetween(start: LocalDate, end: LocalDate): Flow<Double?> =
        reportsDao.getTotalFixedExpensesBetween(start, end)

    fun getTotalPersonalExpensesBetween(start: LocalDate, end: LocalDate): Flow<Double?> =
        reportsDao.getTotalPersonalExpensesBetween(start, end)

    fun getTotalMilkWithFatAndLr(start: LocalDate, end: LocalDate): Flow<Double?> =
        reportsDao.getTotalMilkWithFatAndLrBetween(start, end)


    fun getProfitBetween(start: LocalDate, end: LocalDate): Flow<Double> =
        combine(
            reportsDao.getTotalSalesBetween(start, end),
            reportsDao.getTotalPurchasesBetween(start, end),
            reportsDao.getTotalFixedExpensesBetween(start, end)
        ) { sales, purchases, expenses ->
            val totalSales = sales ?: 0.0
            val totalPurchases = purchases ?: 0.0
            val totalExpenses = expenses ?: 0.0
            totalSales - (totalPurchases + totalExpenses)
        }

    // ───────────────────────────────
    // 📊 ALL RECORDS QUERIES
    // ───────────────────────────────

    fun getTotalFat(): Flow<Double?> = reportsDao.getTotalFat()
    fun getTotalLr(): Flow<Double?> = reportsDao.getTotalLr()
    fun getTotalTs(): Flow<Double?> = reportsDao.getTotalTs()


    fun getTotalMilkPurchaseAll(): Flow<Double?> =
        reportsDao.getTotalMilkPurchaseAll()

    fun getTotalMilkSoldAll(): Flow<Double?> =
        reportsDao.getTotalMilkSoldAll()

    fun getTotalSalesAll(): Flow<Double?> =
        reportsDao.getTotalSalesAll()

    fun getTotalPurchasesAll(): Flow<Double?> =
        reportsDao.getTotalPurchasesAll()

    fun getTotalFixedExpenses(): Flow<Double?> =
        reportsDao.getTotalFixedExpensesAll()

    fun getTotalPersonalExpense(): Flow<Double?> = reportsDao.getTotalPersonalExpensesAll()


    fun getTotalSalesDaily(date: LocalDate): Flow<Double?> =
        reportsDao.getTotalSalesDaily(date)

    fun getTotalPurchasesDaily(date: LocalDate): Flow<Double?> =
        reportsDao.getTotalPurchasesDaily(date)

    fun getTotalExpensesDaily(date: LocalDate): Flow<Double?> =
        reportsDao.getTotalExpensesDaily(date)



    fun getProfitAll(): Flow<Double> =
        combine(
            reportsDao.getTotalSalesAll(),
            reportsDao.getTotalPurchasesAll(),
            reportsDao.getTotalFixedExpensesAll()
        ) { sales, purchases, expenses ->
            val totalSales = sales ?: 0.0
            val totalPurchases = purchases ?: 0.0
            val totalExpenses = expenses ?: 0.0
            totalSales - (totalPurchases + totalExpenses)
        }
}

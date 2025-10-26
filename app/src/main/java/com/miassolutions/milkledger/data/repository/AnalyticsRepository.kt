package com.miassolutions.milkledger.data.repository


import com.miassolutions.milkledger.data.local.daos.ExpensesDao
import com.miassolutions.milkledger.data.local.daos.PurchaseDao
import com.miassolutions.milkledger.data.local.daos.SalesDao
import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.time.LocalDate

@Singleton
class AnalyticsRepository @Inject constructor(
    private val salesDao: SalesDao,
    private val purchaseDao: PurchaseDao,
    private val expensesDao: ExpensesDao
) {

    suspend fun getSalesTotal(start: LocalDate, end: LocalDate): Double =
        salesDao.getSalesTotalBetween(start, end) ?: 0.0

    suspend fun getPurchasesTotal(start: LocalDate, end: LocalDate): Double =
        purchaseDao.getPurchasesTotalBetween(start, end) ?: 0.0

    suspend fun getExpensesTotal(start: LocalDate, end: LocalDate): Double =
        expensesDao.getExpensesTotalBetween(start, end) ?: 0.0

    suspend fun getProfit(start: LocalDate, end: LocalDate): Double {
        val sales = getSalesTotal(start, end)
        val purchases = getPurchasesTotal(start, end)
        val expenses = getExpensesTotal(start, end)
        return sales - (purchases + expenses)
    }
}

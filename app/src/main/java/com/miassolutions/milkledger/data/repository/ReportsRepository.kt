package com.miassolutions.milkledger.data.repository


import com.miassolutions.milkledger.core.extensions.toDisplayDate
import com.miassolutions.milkledger.core.extensions.toDisplayFormat
import com.miassolutions.milkledger.core.extensions.toMillis
import com.miassolutions.milkledger.data.local.daos.ReportsDao
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class ReportsRepository @Inject constructor(
    private val reportsDao: ReportsDao
) {

    fun getTotalMilkPurchaseBetween(start: LocalDate, end: LocalDate): Flow<Double?> =
        reportsDao.getTotalMilkPurchaseBetween(start.toMillis(), end.toMillis())

    fun getTotalMilkSoldBetween(start: LocalDate, end: LocalDate): Flow<Double?> =
        reportsDao.getTotalMilkSoldBetween(start.toMillis(), end.toMillis())

    fun getTotalSalesBetween(start: LocalDate, end: LocalDate): Flow<Double?> =
        reportsDao.getTotalSalesBetween(start.toMillis(), end.toMillis())

    fun getTotalPurchasesBetween(start: LocalDate, end: LocalDate): Flow<Double?> =
        reportsDao.getTotalPurchasesBetween(start.toMillis(), end.toMillis())

    fun getTotalExpensesBetween(start: LocalDate, end: LocalDate): Flow<Double?> =
        reportsDao.getTotalExpensesBetween(start.toMillis(), end.toMillis())

    fun getProfitBetween(start: LocalDate, end: LocalDate): Flow<Double> =
        combine(
            reportsDao.getTotalSalesBetween(start.toMillis(), end.toMillis()),
            reportsDao.getTotalPurchasesBetween(start.toMillis(), end.toMillis()),
            reportsDao.getTotalExpensesBetween(start.toMillis(), end.toMillis())
        ) { sales, purchases, expenses ->
            val totalSales = sales ?: 0.0
            val totalPurchases = purchases ?: 0.0
            val totalExpenses = expenses ?: 0.0
            totalSales - (totalPurchases + totalExpenses)
        }
}

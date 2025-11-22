package com.miassolutions.milkledger.presentation.stats


import com.miassolutions.milkledger.data.local.daos.StateDao
import javax.inject.Inject
import javax.inject.Singleton
import java.time.LocalDate

@Singleton
class StatsRepository @Inject constructor(
    private val dao: StateDao
) {

    suspend fun getTotalsForRange(start: LocalDate, end: LocalDate): List<StateRecord> {
        val customers = dao.getCustomerTotals(start, end)
        val suppliers = dao.getSupplierTotals(start, end)
        val expenses = dao.getExpenseTotals(start, end)

        return buildList {
            addAll(customers)
            addAll(suppliers)
            addAll(expenses)
        }
    }

    suspend fun getDaily(date: LocalDate) =
        getTotalsForRange(date, date)

    suspend fun getWeekly(range: Pair<LocalDate, LocalDate>) =
        getTotalsForRange(range.first, range.second)

    suspend fun getMonthly(range: Pair<LocalDate, LocalDate>) =
        getTotalsForRange(range.first, range.second)

    suspend fun getYearly(range: Pair<LocalDate, LocalDate>) =
        getTotalsForRange(range.first, range.second)

    suspend fun getCustom(start: LocalDate, end: LocalDate) =
        getTotalsForRange(start, end)
}

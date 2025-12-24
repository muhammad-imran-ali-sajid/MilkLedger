package com.miassolutions.milkledger.presentation.stats


import com.miassolutions.milkledger.data.local.daos.StatsDao
import javax.inject.Inject
import javax.inject.Singleton
import java.time.LocalDate

@Singleton
class StatsRepository @Inject constructor(
    private val dao: StatsDao
) {

    suspend fun getTotalsForRange(start: LocalDate, end: LocalDate): List<StateRecord> {
        val customers = dao.getCustomerTotals(start, end)
        val suppliers = dao.getSupplierTotals(start, end)
        val expenses = dao.getBusinessExpenseTotals(start, end)
        val personalExpenses = dao.getPersonalExpenseTotals(start, end)
        val profits = dao.getProfitTotals(start, end)

        return buildList {
            addAll(customers)
            addAll(suppliers)
            addAll(expenses)
            addAll(personalExpenses)
            addAll(profits)
        }
    }


}

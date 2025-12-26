package com.miassolutions.milkledger.presentation.stats


import com.miassolutions.milkledger.core.extensions.toMillis
import com.miassolutions.milkledger.data.local.daos.StatsDao
import javax.inject.Inject
import javax.inject.Singleton
import java.time.LocalDate

@Singleton
class StatsRepository @Inject constructor(
    private val dao: StatsDao
) {

    suspend fun getTotalsForRange(start: LocalDate, end: LocalDate): List<StateRecord> {
        val customers = dao.getCustomerTotals(start.toMillis(), end.toMillis())
        val suppliers = dao.getSupplierTotals(start.toMillis(), end.toMillis())
        val expenses = dao.getBusinessExpenseTotals(start.toMillis(), end.toMillis())
        val personalExpenses = dao.getPersonalExpenseTotals(start.toMillis(), end.toMillis())
        val profits = dao.getProfitTotals(start.toMillis(), end.toMillis())

        return buildList {
            addAll(customers)
            addAll(suppliers)
            addAll(expenses)
            addAll(personalExpenses)
            addAll(profits)
        }
    }


}

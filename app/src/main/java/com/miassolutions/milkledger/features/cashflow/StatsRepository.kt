package com.miassolutions.milkledger.features.cashflow


import com.miassolutions.milkledger.features.transaction.data.TransactionDao
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StatsRepository @Inject constructor(
    private val dao: TransactionDao
) {

//    suspend fun getTotalsForRange(start: LocalDate, end: LocalDate): List<StateRecord> {
//        val customers = dao.getCustomerTotals(start.toMillis(), end.toMillis())
//        val suppliers = dao.getSupplierTotals(start.toMillis(), end.toMillis())
//        val expenses = dao.getBusinessExpenseTotals(start.toMillis(), end.toMillis())
//        val personalExpenses = dao.getPersonalExpenseTotals(start.toMillis(), end.toMillis())
//        val profits = dao.getCustomerTotals(start.toMillis(), end.toMillis()) //temp todo()
//
//        return buildList {
//            addAll(customers)
//            addAll(suppliers)
//            addAll(expenses)
//            addAll(personalExpenses)
//            addAll(profits)
//        }
//    }


}

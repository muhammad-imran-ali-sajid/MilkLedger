package com.miassolutions.milkledger.presentation.profit

import com.miassolutions.milkledger.data.local.daos.ExpensesDao
import com.miassolutions.milkledger.data.local.daos.PurchaseDao
import com.miassolutions.milkledger.data.local.daos.SalesDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject


class StatisticsRepository @Inject constructor(

    private val purchaseDao: PurchaseDao,
    private val salesDao: SalesDao,
    private val expensesDao: ExpensesDao
) {


    fun observeNetProfit(): Flow<Double> = combine(
        salesDao.observeSales(),
        purchaseDao.observePurchases(),
        expensesDao.observeBusinessExpenses(),
    ) { income, cost, businessExp ->

        income - cost - businessExp

    }


    fun observeProfitAfterPersonalExp(): Flow<Double> = combine(
        salesDao.observeSales(),
        purchaseDao.observePurchases(),
        expensesDao.observeBusinessExpenses(),
        expensesDao.observePersonalExpenses()
    ) { income, cost, businessExp, personalExp ->

        income - cost - businessExp - personalExp

    }
}
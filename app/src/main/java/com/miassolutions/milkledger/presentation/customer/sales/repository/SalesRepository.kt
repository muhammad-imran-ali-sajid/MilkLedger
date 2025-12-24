package com.miassolutions.milkledger.presentation.customer.sales.repository

import com.miassolutions.milkledger.presentation.customer.sales.db.SalesDao
import com.miassolutions.milkledger.data.local.entities.CustomerEntity
import com.miassolutions.milkledger.presentation.customer.sales.db.SalesEntity
import com.miassolutions.milkledger.data.local.relations.SaleWithCustomer
import com.miassolutions.milkledger.presentation.stats.CustomerPaidSummary
import com.miassolutions.milkledger.presentation.supplier.BalanceHistory
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SalesRepository @Inject constructor(
    private val salesDao: SalesDao,
) {
    fun getPaidSalesForDate(targetDate: LocalDate): Flow<List<CustomerPaidSummary>> {
                return salesDao.getPaidAmountForDate(targetDate)
    }

    suspend fun getBalanceHistory(customerId: String): List<BalanceHistory> {
        return salesDao.getCustomerBalanceHistory(customerId)
    }

    suspend fun getBalanceHistoryOnce(customerId: String): List<SaleWithCustomer> {
        return salesDao.getCustomerBalanceHistoryOnce(customerId)
    }

    fun observeCustomersList(): Flow<List<CustomerEntity>> = salesDao.observeCustomersList()

    suspend fun isDuplicateSale(customerId: String, date: LocalDate): Boolean =
        salesDao.countSalesForDate(customerId, date) > 0
    suspend fun insertSale(sale: SalesEntity) {
              salesDao.upsertAll(listOf(sale))
    }


    suspend fun updateSale(sale: SalesEntity) {
        salesDao.upsertAll(listOf(sale)) // Using upsertAll
    }

    suspend fun deleteSale(saleId: String) {
        salesDao.deleteSale(saleId)
    }

    fun getSalesByDate(date: LocalDate): Flow<List<SaleWithCustomer>> =
        salesDao.getSalesByDate(date)

    fun getSalesForCustomer(customerId: String): Flow<List<SaleWithCustomer>> =
        salesDao.getSalesForCustomer(customerId)

}
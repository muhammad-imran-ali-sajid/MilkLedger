package com.miassolutions.milkledger.data.repository

import com.miassolutions.milkledger.core.extensions.toMillis
import com.miassolutions.milkledger.data.local.daos.SalesDao
import com.miassolutions.milkledger.data.local.daos.TransactionDao
import com.miassolutions.milkledger.data.local.entities.SalesEntity
import com.miassolutions.milkledger.data.local.entities.TransactionEntity
import com.miassolutions.milkledger.data.local.entities.TransactionType
import com.miassolutions.milkledger.data.mapper.toDomain
import com.miassolutions.milkledger.data.mapper.toEntity
import com.miassolutions.milkledger.data.remote.FirestoreSyncHelper
import com.miassolutions.milkledger.domain.model.Sale
import com.miassolutions.milkledger.presentation.stats.CustomerPaidSummary
import com.miassolutions.milkledger.presentation.supplier.BalanceHistory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SalesRepository @Inject constructor(
    private val salesDao: SalesDao,
    private val transactionDao: TransactionDao,
    private val firestore: FirestoreSyncHelper
) {

    companion object {
        private const val COLLECTION = "sales"
        private const val TAG = "SalesRepository"
    }

    fun getPaisSalesForDate(date: LocalDate): Flow<List<CustomerPaidSummary>> =
        salesDao.getPaidAmountForDate(date.toMillis())

    fun getSalesByDate(date: LocalDate): Flow<List<Sale>> =
        salesDao.getSalesByDate(date.toMillis())
            .map { list ->
                list.map {
                    it.sale.toDomain()
                }
            }

    fun getSalesForCustomer(customerId: String): Flow<List<Sale>> =
        salesDao.getSalesForCustomer(customerId)
            .map { list -> list.map { it.sale.toDomain() } }


    suspend fun getBalanceHistory(customerId: String): List<BalanceHistory> =
        salesDao.getCustomerBalanceHistory(customerId)

    suspend fun isDuplicateSale(customerId: String, date: LocalDate): Boolean =
        salesDao.countSalesForDate(customerId, date.toMillis()) > 0

    private fun calculateProfit(sale: SalesEntity) : Double {
        return sale.price - sale.balance
    }
    suspend fun insertSale(sale: Sale){
        val entity = sale.toEntity()

        salesDao.insertSale(entity)

        val transaction = TransactionEntity(
            dateMillis = entity.dateMillis,
            type = TransactionType.SALE,
            referenceId = entity.saleId,
            debit = 0.0,
            credit = entity.paid,
            profitImpact = calculateProfit(entity),
            note = "Sale to customer ${entity.customerId}"
        )
    }
}
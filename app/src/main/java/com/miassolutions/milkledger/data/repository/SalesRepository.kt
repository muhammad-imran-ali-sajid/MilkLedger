package com.miassolutions.milkledger.data.repository



import com.miassolutions.milkledger.data.local.daos.SalesEntryDao
import com.miassolutions.milkledger.data.local.entities.SalesEntryEntity
import com.miassolutions.milkledger.data.local.relations.SaleWithCustomer
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SalesRepository @Inject constructor(
    private val salesEntryDao: SalesEntryDao
) {

    fun getAllSalesWithCustomers(): Flow<List<SaleWithCustomer>> =
        salesEntryDao.getAllSalesWithCustomers()

    fun getSalesByDate(date: LocalDate): Flow<List<SaleWithCustomer>> =
        salesEntryDao.getSalesByDate(date)

    fun getSalesForCustomer(customerId: String): Flow<List<SaleWithCustomer>> =
        salesEntryDao.getSalesForCustomer(customerId)

    suspend fun insertSale(sale: SalesEntryEntity) =
        salesEntryDao.insertSale(sale)

    suspend fun deleteSale(saleId: String) =
        salesEntryDao.deleteSale(saleId)
}

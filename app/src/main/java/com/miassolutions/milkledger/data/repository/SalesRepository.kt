package com.miassolutions.milkledger.data.repositories

import com.miassolutions.milkledger.data.local.daos.SalesDao
import com.miassolutions.milkledger.data.local.entities.CustomerEntity
import com.miassolutions.milkledger.data.local.entities.SalesEntity
import com.miassolutions.milkledger.data.local.relations.SaleWithCustomer
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SalesRepository @Inject constructor(
    private val salesDao: SalesDao
) {

    // ✅ One-time fetch for exporting or single-use operations
    suspend fun getSalesByDateOnce(date: LocalDate): List<SaleWithCustomer> =
        salesDao.getSalesByDateOnce(date)

    fun getAllCustomers() : Flow<List<CustomerEntity>> =
        salesDao.getAllCustomers()


    // 🧾 All sales for reports or admin
    fun getAllSalesWithCustomers(): Flow<List<SaleWithCustomer>> =
        salesDao.getAllSalesWithCustomers()

    // 📅 For current date screen (daily ledger)
    fun getSalesByDate(date: LocalDate): Flow<List<SaleWithCustomer>> =
        salesDao.getSalesByDate(date)

    // 👤 For customer ledger details
    fun getSalesForCustomer(customerId: String): Flow<List<SaleWithCustomer>> =
        salesDao.getSalesForCustomer(customerId)

    // 🟢 Insert new sale (when a customer is first added today)
    suspend fun insertSale(sale: SalesEntity) =
        salesDao.insertSale(sale)

    // 🟡 Update live changes (fat, lr, volume, notes, etc.)
    suspend fun updateSale(sale: SalesEntity) =
        salesDao.updateSale(sale)

    // 🔴 Delete sale entry
    suspend fun deleteSale(saleId: String) =
        salesDao.deleteSale(saleId)
}

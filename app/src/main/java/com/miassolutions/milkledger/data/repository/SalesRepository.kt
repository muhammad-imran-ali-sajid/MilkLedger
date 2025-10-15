package com.miassolutions.milkledger.data.repositories

import com.miassolutions.milkledger.data.local.daos.SalesEntryDao
import com.miassolutions.milkledger.data.local.entities.CustomerEntity
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

    // ✅ One-time fetch for exporting or single-use operations
    suspend fun getSalesByDateOnce(date: LocalDate): List<SaleWithCustomer> =
        salesEntryDao.getSalesByDateOnce(date)

    fun getAllCustomers() : Flow<List<CustomerEntity>> =
        salesEntryDao.getAllCustomers()


    // 🧾 All sales for reports or admin
    fun getAllSalesWithCustomers(): Flow<List<SaleWithCustomer>> =
        salesEntryDao.getAllSalesWithCustomers()

    // 📅 For current date screen (daily ledger)
    fun getSalesByDate(date: LocalDate): Flow<List<SaleWithCustomer>> =
        salesEntryDao.getSalesByDate(date)

    // 👤 For customer ledger details
    fun getSalesForCustomer(customerId: String): Flow<List<SaleWithCustomer>> =
        salesEntryDao.getSalesForCustomer(customerId)

    // 🟢 Insert new sale (when a customer is first added today)
    suspend fun insertSale(sale: SalesEntryEntity) =
        salesEntryDao.insertSale(sale)

    // 🟡 Update live changes (fat, lr, volume, notes, etc.)
    suspend fun updateSale(sale: SalesEntryEntity) =
        salesEntryDao.updateSale(sale)

    // 🔴 Delete sale entry
    suspend fun deleteSale(saleId: String) =
        salesEntryDao.deleteSale(saleId)
}

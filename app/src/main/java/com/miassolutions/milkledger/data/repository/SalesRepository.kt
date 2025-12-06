package com.miassolutions.milkledger.data.repository

import android.util.Log
import com.miassolutions.milkledger.data.local.daos.SalesDao
import com.miassolutions.milkledger.data.local.entities.CustomerEntity
import com.miassolutions.milkledger.data.local.entities.SalesEntity
import com.miassolutions.milkledger.data.local.relations.SaleWithCustomer
import com.miassolutions.milkledger.data.mapper.toFirestoreModel
import com.miassolutions.milkledger.data.remote.FirestoreSyncHelper
import com.miassolutions.milkledger.presentation.stats.CustomerPaidSummary
import com.miassolutions.milkledger.presentation.supplier.BalanceHistory
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SalesRepository @Inject constructor(
    private val salesDao: SalesDao,
    // Keep FirestoreSyncHelper for W/U/D operations (local-write-first, remote-write-after)
    private val firestoreSyncHelper: FirestoreSyncHelper
) {

    fun observeCustomerRate(id: String) : Flow<Double> = salesDao.observeCustomerRate(id)

    fun getPaidSalesForDate(targetDate: LocalDate): Flow<List<CustomerPaidSummary>> {
        // Simple pass-through call to the DAO
        return salesDao.getPaidAmountForDate(targetDate)
    }

    suspend fun getBalanceHistory(customerId: String): List<BalanceHistory> {
        return salesDao.getCustomerBalanceHistory(customerId)
    }

    fun observeCustomersList(): Flow<List<CustomerEntity>> = salesDao.observeCustomersList()

    suspend fun isDuplicateSale(customerId: String, date: LocalDate): Boolean =
        salesDao.countSalesForDate(customerId, date) > 0


    private companion object {
        private const val TAG = "SalesRepository"
        private const val SALES_COLLECTION = "sales"
    }

    // --- Write/Update/Delete Operations (Local Write First, Then Remote Sync) ---
    // These remain the same as they correctly handle the local write and remote *push*.

    /**
     * Inserts a new sale: Local write first, then initiate remote sync.
     */
    suspend fun insertSale(sale: SalesEntity) {
        // 1. Local write for immediate UI update (will trigger flow collectors)
        salesDao.upsertAll(listOf(sale)) // Using upsertAll to align with DataRepository's logic
        Log.d(TAG, "Inserted sale locally: ${sale.saleId}")

        // 2. Initiate remote write
        try {
            val firestoreSale = sale.toFirestoreModel()
            firestoreSyncHelper.uploadSingle(
                SALES_COLLECTION, documentId = sale.saleId,
                data = firestoreSale
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync inserted sale: ${e.message}")
        }
    }

    /**
     * Updates an existing sale: Local write first, then initiate remote sync.
     */
    suspend fun updateSale(sale: SalesEntity) {
        // 1. Local write for immediate UI update
        salesDao.upsertAll(listOf(sale)) // Using upsertAll
        Log.d(TAG, "Updated sale locally: ${sale.saleId}")

        // 2. Initiate remote write
        try {
            val firestoreSale = sale.toFirestoreModel()
            firestoreSyncHelper.uploadSingle(
                SALES_COLLECTION, documentId = sale.saleId,
                data = firestoreSale
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync updated sale: ${e.message}")
        }
    }

    /**
     * Deletes a sale: Local delete first, then initiate remote sync.
     */
    suspend fun deleteSale(saleId: String) {
        // 1. Local delete for immediate UI update
        // NOTE: This requires salesDao.deleteById(id: String) to be implemented.
        // I will assume you implement a 'deleteById' function in SalesDao.
        salesDao.deleteSale(saleId)
        Log.d(TAG, "Deleted sale locally: $saleId")


        // 2. Initiate remote delete
        try {
            if (saleId.isNotBlank()) {
                firestoreSyncHelper.deleteDocument(SALES_COLLECTION, saleId)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync delete sale: ${e.message}")
        }
    }

    // --- Bulk Sync Operations (Removed/Replaced) ---

    /**
     * Batch inserts or replaces (upserts) a collection of sales into the local database.
     * This is only needed for the DataRepository to call, NOT for the SalesRepository itself.
     * We keep it here as a public function for the DataRepository to use.
     */
    suspend fun upsertAllSales(sales: List<SalesEntity>) {
        salesDao.upsertAll(sales)
    }

    /**
     * 🔥 REMOVED: SynchronizeSales()
     * The continuous read-sync (download) is handled by DataRepository.setupRealtimeListeners().
     * The write-sync (upload) is handled by insertSale/updateSale/deleteSale.
     * A full two-way sync function is no longer needed with real-time listeners.
     */


    // --- Read Operations (Remain purely local via Room/Flow) ---

    // ✅ One-time fetch for exporting or single-use operations
    suspend fun getSalesByDateOnce(date: LocalDate): List<SaleWithCustomer> =
        salesDao.getSalesByDateOnce(date)

    // 🧾 All sales for reports or admin
    fun getAllSalesWithCustomers(): Flow<List<SaleWithCustomer>> =
        salesDao.getAllSalesWithCustomers()

    // 📅 For current date screen (daily ledger)
    fun getSalesByDate(date: LocalDate): Flow<List<SaleWithCustomer>> =
        salesDao.getSalesByDate(date)

    // 👤 For customer ledger details
    fun getSalesForCustomer(customerId: String): Flow<List<SaleWithCustomer>> =
        salesDao.getSalesForCustomer(customerId)

    // Assuming CustomerEntity is managed by another repository but needed here for convenience
    fun getAllCustomers(): Flow<List<CustomerEntity>> =
        salesDao.getAllCustomers()
    // NOTE: This dao function would need to exist on SalesDao or be injected from CustomerDao
}
package com.miassolutions.milkledger.data.repository

import android.util.Log // Added Log import
import com.miassolutions.milkledger.data.local.daos.SalesDao
import com.miassolutions.milkledger.data.local.entities.CustomerEntity
import com.miassolutions.milkledger.data.local.entities.SalesEntity
import com.miassolutions.milkledger.data.local.relations.SaleWithCustomer
import com.miassolutions.milkledger.data.remote.FirestoreSyncHelper // Assuming this path
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SalesRepository @Inject constructor(
    private val salesDao: SalesDao,
    private val firestoreSyncHelper: FirestoreSyncHelper // Inject the sync helper
) {

    private companion object {
        private const val TAG = "SalesRepository"
        // Define the Firestore collection name for sales
        private const val SALES_COLLECTION = "sales"
    }

    // --- Write/Update/Delete Operations (Local Write First, Then Remote Sync) ---

    /**
     * Inserts a new sale: Local write first, then initiate remote sync.
     */
    suspend fun insertSale(sale: SalesEntity) {
        // 1. Local write for immediate UI update
        salesDao.insertSale(sale)

        // 2. Initiate remote write (to local cache, then syncs to remote)
        try {
            // Assumes SalesEntity has a unique 'saleId' for the document ID
            firestoreSyncHelper.uploadSingle(SALES_COLLECTION, sale.saleId, sale)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync inserted sale: ${e.message}")
            // Log the error but allow the local operation to succeed (offline-first)
        }
    }

    /**
     * Updates an existing sale: Local write first, then initiate remote sync.
     */
    suspend fun updateSale(sale: SalesEntity) {
        // 1. Local write for immediate UI update
        salesDao.updateSale(sale)

        // 2. Initiate remote write
        try {
            // Requires the entity to have a valid saleId for successful remote update
            firestoreSyncHelper.uploadSingle(SALES_COLLECTION, sale.saleId, sale)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync updated sale: ${e.message}")
        }
    }

    /**
     * Deletes a sale: Local delete first, then initiate remote sync.
     */
    suspend fun deleteSale(saleId: String) {
        // Find the sale locally (optional, but ensures we have the ID if we only passed the entity)
        // For simplicity, we assume the caller provides the ID, but a full delete should track the entity.
        // salesDao.deleteSale(saleId) // This is the local delete

        // 1. Local delete for immediate UI update
        salesDao.deleteSale(saleId)

        // 2. Initiate remote delete
        try {
            if (saleId.isNotBlank()) {
                firestoreSyncHelper.deleteDocument(SALES_COLLECTION, saleId)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync delete sale: ${e.message}")
        }
    }


    // --- Bulk Sync Operations ---

    /**
     * Batch inserts or replaces (upserts) a collection of sales into the local database.
     * This is typically used after a remote synchronization pull.
     */
    suspend fun upsertAllSales(sales: List<SalesEntity>) {
        // Assuming SalesDao has an upsertAll method like your SupplierDao
        salesDao.upsertAll(sales)
    }

    /**
     * Synchronizes all local sales with the remote Firestore database.
     * This performs a full two-way sync: downloads remote changes and uploads all local changes.
     */
    suspend fun synchronizeSales() {
        Log.d(TAG, "Starting full sale synchronization...")
        try {
            // 1. Download and merge remote changes
            val remoteSales = firestoreSyncHelper.downloadCollection<SalesEntity>(
                SALES_COLLECTION
            )

            if (remoteSales.isNotEmpty()) {
                // Insert/replace all downloaded data into the local Room database
                salesDao.upsertAll(remoteSales)
                Log.d(TAG, "Downloaded and merged ${remoteSales.size} sales from Firestore.")
            } else {
                Log.d(TAG, "No remote items found to download.")
            }

            // 2. Upload all local changes
            // NOTE: This assumes the DAO has a function to get all *entities* as a List
            val allLocalSales = salesDao.getAllSalesList()
            if (allLocalSales.isNotEmpty()) {
                firestoreSyncHelper.uploadCollection(
                    collectionName = SALES_COLLECTION,
                    dataList = allLocalSales,
                    idExtractor = { it.saleId } // Explicitly use the 'saleId' field
                )
                Log.d(TAG, "Uploaded ${allLocalSales.size} local items to Firestore.")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Full sale synchronization failed: ${e.localizedMessage}", e)
        }
    }


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
    fun getAllCustomers() : Flow<List<CustomerEntity>> =
        salesDao.getAllCustomers()

    // NOTE: salesDao would need a suspend fun getAllSalesList(): List<SalesEntity> for the sync.
    // NOTE: salesDao would need a suspend fun upsertAll(sales: List<SalesEntity>) for the sync.
}
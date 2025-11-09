package com.miassolutions.milkledger.data.repository

import android.util.Log
import com.miassolutions.milkledger.data.local.daos.CustomerDao
import com.miassolutions.milkledger.data.local.daos.SupplierDao
import com.miassolutions.milkledger.data.mapper.toRoomEntity
import com.miassolutions.milkledger.data.remote.FirestoreSyncHelper
import com.miassolutions.milkledger.data.remote.model.FirestoreCustomer
import com.miassolutions.milkledger.data.remote.model.FirestoreSupplier
import jakarta.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

// DataRepository.kt (Refined)

class DataRepository @Inject constructor(
    private val syncHelper: FirestoreSyncHelper,
    private val supplierDao: SupplierDao, // Injected Room DAO
    private val customerDao: CustomerDao,
    // ... inject other DAOs here later (e.g., productDao)
) {
    // ... AppData, toRoomEntity() mapper (as extension function) ...

    /**
     * Downloads ALL collections required for the app and saves them to Room.
     */
    suspend fun syncAllDataFromCloud() = coroutineScope {
        // --- 1. DOWNLOAD SUPPLIERS ---
        val suppliersDeferred = async {
            syncHelper.downloadCollection<FirestoreSupplier>("suppliers")
        }

        val customersDeferred = async {
            syncHelper.downloadCollection<FirestoreCustomer>("customers")
        }

        // --- 2. DOWNLOAD OTHER COLLECTIONS HERE (e.g., Products) ---
        // val productsDeferred = async {
        //     syncHelper.downloadCollection<FirestoreProduct>("products")
        // }

        // Await all downloads concurrently
        val downloadedSuppliers = suppliersDeferred.await()
        // val downloadedProducts = productsDeferred.await()
        val downloadedCustomers = customersDeferred.await()

        // --- 3. PROCESS AND SAVE SUPPLIERS ---
        val supplierEntities = downloadedSuppliers.map { it.toRoomEntity() }
        supplierDao.upsertAll(supplierEntities)
        Log.d("DataRepository", "Saved ${supplierEntities.size} suppliers to Room.")

        val customersEntities = downloadedCustomers.map { it.toRoomEntity() }
        customerDao.upsertAll(customersEntities)
        Log.d("DataRepository", "Saved ${customersEntities.size} suppliers to Room.")


        // --- 4. PROCESS AND SAVE PRODUCTS ---
        // val productEntities = downloadedProducts.map { it.toRoomEntity() }
        // productDao.upsertAll(productEntities)
        // Log.d("DataRepository", "Saved ${productEntities.size} products to Room.")

        // Return a success status or nothing
        return@coroutineScope // Signifies completion
    }
}


// A container to hold all fetched data
data class AppData(
    val suppliers: List<FirestoreSupplier>,
    val customers: List<FirestoreCustomer>
)
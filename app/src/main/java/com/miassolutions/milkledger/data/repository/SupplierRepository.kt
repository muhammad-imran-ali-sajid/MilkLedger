package com.miassolutions.milkledger.data.repository


import android.util.Log
import com.google.firebase.firestore.Source
import com.miassolutions.milkledger.data.local.daos.PurchaseDao
import com.miassolutions.milkledger.data.local.daos.SupplierDao
import com.miassolutions.milkledger.data.local.entities.SupplierEntity
import com.miassolutions.milkledger.data.remote.FirestoreSyncHelper
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SupplierRepository @Inject constructor(
    private val supplierDao: SupplierDao,
    private val purchaseDao: PurchaseDao,
    private val firestoreSyncHelper: FirestoreSyncHelper // Inject the sync helper
) {

    private companion object {
        private const val TAG = "SupplierRepository"
        // Define the Firestore collection name for suppliers
        private const val SUPPLIER_COLLECTION = "suppliers"
    }

    /**
     * Batch inserts or replaces (upserts) a collection of suppliers into the local database.
     * This is typically used after a remote synchronization pull.
     * NOTE: This assumes the SupplierDao has an efficient upsert implementation (e.g., using @Insert(onConflict = REPLACE)).
     */
    suspend fun upsertAllSuppliers(suppliers: List<SupplierEntity>) {
        supplierDao.upsertAll(suppliers)
    }


    /**
     * Insert or replace a supplier: Local write first, then initiate remote sync.
     */
    suspend fun insertSupplier(supplier: SupplierEntity) {
        // 1. Local write for immediate UI update
        supplierDao.insertSupplier(supplier)

        // 2. Initiate remote write (happens to local cache, then syncs to remote)
        try {
            // Assumes SupplierEntity has a field that can be used as the document ID
            firestoreSyncHelper.uploadSingle(SUPPLIER_COLLECTION, supplier.supplierId, supplier)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync inserted supplier: ${e.message}")
            // Log the error but allow the local operation to succeed (offline-first)
        }
    }

    /**
     * Update an existing supplier: Local write first, then initiate remote sync.
     */
    suspend fun updateSupplier(supplier: SupplierEntity) {
        // 1. Local write for immediate UI update
        supplierDao.updateSupplier(supplier)

        // 2. Initiate remote write
        try {
            // Requires the entity to have a valid ID for successful remote update
            firestoreSyncHelper.uploadSingle(SUPPLIER_COLLECTION, supplier.supplierId, supplier)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync updated supplier: ${e.message}")
        }
    }

    /**
     * Delete a supplier: Local delete first, then initiate remote sync.
     */
    suspend fun deleteSupplier(supplier: SupplierEntity) {
        // 1. Local delete for immediate UI update
        supplierDao.deleteSupplier(supplier)

        // 2. Initiate remote delete
        try {
            if (supplier.supplierId.isNotBlank()) {
                firestoreSyncHelper.deleteDocument(SUPPLIER_COLLECTION, supplier.supplierId)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync delete supplier: ${e.message}")
        }
    }

    /**
     * Synchronizes all local suppliers with the remote Firestore database.
     * This performs a full two-way sync: downloads remote changes and uploads all local changes.
     */
    suspend fun synchronizeSuppliers() {
        Log.d(TAG, "Starting full supplier synchronization...")
        try {
            // 1. Download and merge remote changes
            // Use Source.DEFAULT to request network data, falling back to cache if offline.
            val remoteSuppliers = firestoreSyncHelper.downloadCollection<SupplierEntity>(
                SUPPLIER_COLLECTION
            )

            if (remoteSuppliers.isNotEmpty()) {
                // Insert/replace all downloaded data into the local Room database
                supplierDao.upsertAll(remoteSuppliers)
                Log.d(TAG, "Downloaded and merged ${remoteSuppliers.size} suppliers from Firestore.")
            } else {
                Log.d(TAG, "No remote items found to download.")
            }

            // 2. Upload all local changes (ensuring all local data is pushed)
            // NOTE: This assumes the DAO has a function to get all entities as a List
            val allLocalSuppliers = supplierDao.getAllSuppliersList()
            if (allLocalSuppliers.isNotEmpty()) {
                firestoreSyncHelper.uploadCollection(
                    collectionName = SUPPLIER_COLLECTION,
                    dataList = allLocalSuppliers,
                    idExtractor = { it.supplierId } // Explicitly use the 'id' field
                )
                Log.d(TAG, "Uploaded ${allLocalSuppliers.size} local items to Firestore.")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Full synchronization failed: ${e.localizedMessage}", e)
            // The system remains operational due to local data, but sync failed.
        }
    }

    // Read operations remain purely local via Room/Flow, observing the local cache (offline-first read pattern).
    fun getAllSuppliers(): Flow<List<SupplierEntity>> {
        return supplierDao.getAllSuppliers()
    }

    // Get a specific supplier by ID
    fun getSupplierById(id: String): Flow<SupplierEntity?> {
        return supplierDao.getSupplierById(id)
    }
}
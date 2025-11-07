package com.miassolutions.milkledger.data.repository

import android.util.Log
import com.google.firebase.firestore.Source
import com.miassolutions.milkledger.data.local.daos.CustomerDao
import com.miassolutions.milkledger.data.local.entities.CustomerEntity
import com.miassolutions.milkledger.data.remote.FirestoreSyncHelper
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CustomerRepository @Inject constructor(
    private val customerDao: CustomerDao,
    private val firestoreHelper: FirestoreSyncHelper
) {

    companion object {
        private const val TAG = "CustomerRepository"
        private const val CUSTOMER_COLLECTION = "customers"
    }

    // --- Local Write Operations + Remote Synchronization Trigger ---

    /**
     * Inserts or replaces a customer locally, then triggers a remote upload.
     * Assumes customerDao.insertCustomer uses OnConflictStrategy.REPLACE (an upsert).
     */
    suspend fun insertCustomer(customer: CustomerEntity) {
        // 1. Local write for immediate UI update
        try {
            customerDao.insertCustomer(customer)
            Log.d(TAG, "Inserted customer locally: ${customer.customerId}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to insert customer locally: ${customer.customerId}", e)
            return
        }

        // 2. Initiate remote write (Firestore handles the background sync)
        try {
            firestoreHelper.uploadSingle(CUSTOMER_COLLECTION, customer.customerId, customer)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync inserted customer to Firestore: ${customer.customerId}", e)
            // Local operation succeeded; remote sync will retry later via the full sync function
        }
    }

    /**
     * Updates an existing customer locally, then triggers a remote update.
     */
    suspend fun updateCustomer(customer: CustomerEntity) {
        // 1. Local write for immediate UI update
        try {
            customerDao.updateCustomer(customer)
            Log.d(TAG, "Updated customer locally: ${customer.customerId}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update customer locally: ${customer.customerId}", e)
            return
        }

        // 2. Initiate remote write
        try {
            firestoreHelper.uploadSingle(CUSTOMER_COLLECTION, customer.customerId, customer)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync updated customer to Firestore: ${customer.customerId}", e)
        }
    }

    /**
     * Deletes a customer locally, then triggers a remote hard-delete.
     * Note: If soft-delete is needed (marking with deletedAt field), the implementation
     * would involve an update/insert instead of a delete. Sticking to the hard-delete pattern
     * for consistency with the established repository model.
     */
    suspend fun deleteCustomer(customer: CustomerEntity) {
        val customerId = customer.customerId

        // 1. Local delete for immediate UI update
        try {
            customerDao.deleteCustomer(customer)
            Log.d(TAG, "Deleted customer locally: $customerId")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete customer locally: $customerId", e)
            return
        }

        // 2. Initiate remote delete
        try {
            firestoreHelper.deleteDocument(CUSTOMER_COLLECTION, customerId)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync delete for customer ID: $customerId", e)
        }
    }

    // --- Full Synchronization ---

    /**
     * Synchronizes all local customers with the remote Firestore database.
     * This performs a full two-way sync: downloads remote changes and uploads all local changes.
     * NOTE: This assumes customerDao.upsertAll and customerDao.getAllCustomersList() exist.
     */
    suspend fun synchronizeCustomers() {
        Log.d(TAG, "Starting full customer synchronization...")
        try {
            // 1. Download and merge remote changes
            // Use Source.DEFAULT to request network data, falling back to cache if offline.
            val remoteCustomers = firestoreHelper.downloadCollection<CustomerEntity>(CUSTOMER_COLLECTION)

            if (remoteCustomers.isNotEmpty()) {
                // Insert/replace all downloaded data into the local Room database
                customerDao.upsertAll(remoteCustomers)
                Log.d(TAG, "Downloaded and merged ${remoteCustomers.size} customers from Firestore.")
            } else {
                Log.d(TAG, "No remote items found to download.")
            }

            // 2. Upload all local changes (ensuring all local data is pushed)
            val allLocalCustomers = customerDao.getAllCustomersList()
            if (allLocalCustomers.isNotEmpty()) {
                firestoreHelper.uploadCollection(
                    collectionName = CUSTOMER_COLLECTION,
                    dataList = allLocalCustomers,
                    idExtractor = { it.customerId } // Explicitly use the 'customerId' field
                )
                Log.d(TAG, "Uploaded ${allLocalCustomers.size} local items to Firestore.")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Full synchronization failed: ${e.localizedMessage}", e)
            // The system remains operational due to local data, but sync failed.
        }
    }

    // --- Local Read Operations (Offline-First Read) ---

    // Get all customers as Flow
    fun getAllCustomers(): Flow<List<CustomerEntity>> = customerDao.getAllCustomers()

    // Get a specific customer by ID as Flow
    fun getCustomerById(id: String): Flow<CustomerEntity?> = customerDao.getCustomerById(id)

    // Get a customer by ID once
    suspend fun getCustomerByIdOnce(id: String): CustomerEntity? = customerDao.getCustomerByIdOnce(id)
}
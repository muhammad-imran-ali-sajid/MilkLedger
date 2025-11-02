package com.miassolutions.milkledger.data.repository

import android.util.Log
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
    }

    private val collectionName = "customers"

    // --- Core CRUD Operations with Improved Sync Logic ---

    /**
     * Insert or replace a customer locally and remotely.
     * Marks the local entity as synced on successful remote upload.
     */
    suspend fun upsertCustomer(customer: CustomerEntity, syncRemote: Boolean = true) {
        val customerToInsert = customer.copy(isSynced = !syncRemote) // Assume not synced if remote sync will happen later
        try {
            customerDao.insertCustomer(customerToInsert)
            Log.d(TAG, "Inserted customer locally: ${customerToInsert.customerId}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to insert customer locally: ${customerToInsert.customerId}", e)
            return // Stop if local insert fails
        }

        if (syncRemote) {
            try {
                firestoreHelper.uploadSingle(collectionName, customer.customerId, customerToInsert)
                // If remote upload succeeds, update local entity's sync status
                customerDao.updateCustomer(customerToInsert.copy(isSynced = true))
                Log.d(TAG, "Synced customer to Firestore: ${customer.customerId} and marked as synced locally.")
            } catch (e: Exception) {
                // The entity remains marked as isSynced = false for syncPendingToFirestore to pick up
                Log.e(TAG, "Failed to sync customer to Firestore: ${customer.customerId}", e)
            }
        }
    }

    /**
     * Update an existing customer locally and remotely.
     * Marks the local entity as synced on successful remote upload.
     */
    suspend fun updateCustomer(customer: CustomerEntity, syncRemote: Boolean = true) {
        val customerToUpdate = customer.copy(isSynced = !syncRemote) // Assume not synced if remote sync will happen later
        try {
            customerDao.updateCustomer(customerToUpdate)
            Log.d(TAG, "Updated customer locally: ${customerToUpdate.customerId}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update customer locally: ${customerToUpdate.customerId}", e)
            return // Stop if local update fails
        }

        if (syncRemote) {
            try {
                firestoreHelper.uploadSingle(collectionName, customer.customerId, customerToUpdate)
                // If remote upload succeeds, update local entity's sync status
                customerDao.updateCustomer(customerToUpdate.copy(isSynced = true))
                Log.d(TAG, "Updated customer in Firestore: ${customer.customerId} and marked as synced locally.")
            } catch (e: Exception) {
                // The entity remains marked as isSynced = false for syncPendingToFirestore to pick up
                Log.e(TAG, "Failed to update customer in Firestore: ${customer.customerId}", e)
            }
        }
    }

    /**
     * Delete a customer: performs remote soft-delete first, then local hard-delete.
     * This prevents re-insertion on refreshFromFirestore if soft-delete fails.
     */
    suspend fun deleteCustomer(customer: CustomerEntity, syncRemote: Boolean = true) {
        if (syncRemote) {
            val deletedCustomer = customer.copy(
                deletedAt = System.currentTimeMillis(),
                isSynced = true // This change must be synced
            )
            try {
                // 1. Perform remote soft-delete
                firestoreHelper.uploadSingle(collectionName, customer.customerId, deletedCustomer)
                Log.d(TAG, "Marked customer as deleted in Firestore: ${customer.customerId}")

                // 2. Perform local hard-delete ONLY if remote soft-delete succeeds
                customerDao.deleteCustomer(customer)
                Log.d(TAG, "Deleted customer locally: ${customer.customerId} after remote sync.")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to mark customer as deleted in Firestore. Local data retained to retry sync.", e)
                // Important: Do not delete locally if remote fails, so the pending status can be re-synced later.
            }
        } else {
            // If syncRemote is false, just delete locally
            try {
                customerDao.deleteCustomer(customer)
                Log.d(TAG, "Deleted customer locally (no remote sync): ${customer.customerId}")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to delete customer locally: ${customer.customerId}", e)
            }
        }
    }

    // --- Data Access Methods (Unchanged) ---

    // Get all customers as Flow
    fun getAllCustomers(): Flow<List<CustomerEntity>> = customerDao.getAllCustomers()

    // Get a specific customer by ID as Flow
    fun getCustomerById(id: String): Flow<CustomerEntity?> = customerDao.getCustomerById(id)

    // Get a customer by ID once
    suspend fun getCustomerByIdOnce(id: String): CustomerEntity? = customerDao.getCustomerByIdOnce(id)

    // --- Bulk Sync Operations ---

    /**
     * Upload pending local changes to Firestore.
     */
    suspend fun syncPendingToFirestore() {
        val pending = customerDao.getPendingSync()
        if (pending.isNotEmpty()) {
            try {
                // Upload the list using the FirestoreSyncHelper
                firestoreHelper.uploadCollection(collectionName, pending) { it.customerId }
                Log.d(TAG, "Successfully synced ${pending.size} pending customers to Firestore")

                // Mark as synced locally using an upsert operation
                val syncedList = pending.map { it.copy(isSynced = true) }
                customerDao.insertAll(syncedList) // Assuming insertAll is an upsert/replace operation
                Log.d(TAG, "Successfully marked ${pending.size} customers as synced locally.")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to sync pending customers to Firestore", e)
            }
        } else {
            Log.d(TAG, "No pending customers to sync")
        }
    }

    /**
     * Download remote Firestore collection and replace local DB.
     * Note: This assumes FirestoreSyncHelper throws on download error.
     */
//    suspend fun refreshFromFirestore() {
//        try {
//            val remoteList: List<CustomerEntity> = firestoreHelper.downloadCollection(collectionName)
//            if (remoteList.isNotEmpty()) {
//                // ⭐️ Use the single atomic DAO function
//                customerDao.replaceAll(remoteList)
//                Log.d(TAG, "Refreshed local DB from Firestore with ${remoteList.size} customers")
//            } else {
//                Log.d(TAG, "No customers found in Firestore to refresh")
//            }
//        } catch (e: Exception) {
//            Log.e(TAG, "Failed to refresh local DB from Firestore. Local data is preserved.", e)
//        }
//    }

    // Download remote Firestore collection and replace local DB
    suspend fun refreshFromFirestore() {
        try {
            val remoteList: List<CustomerEntity> = firestoreHelper.downloadCollection(collectionName)
            if (remoteList.isNotEmpty()) {
                customerDao.clearAll()
                customerDao.insertAll(remoteList)
                Log.d(TAG, "Refreshed local DB from Firestore with ${remoteList.size} customers")
            } else {
                Log.d(TAG, "No customers found in Firestore to refresh")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to refresh local DB from Firestore", e)
        }
    }
}
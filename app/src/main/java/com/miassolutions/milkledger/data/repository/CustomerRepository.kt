package com.miassolutions.milkledger.data.repository

import android.util.Log
import com.miassolutions.milkledger.data.local.daos.CustomerDao
import com.miassolutions.milkledger.data.local.entities.CustomerEntity
import com.miassolutions.milkledger.data.mapper.toEntity
import com.miassolutions.milkledger.data.mapper.toFirestoreModel
import com.miassolutions.milkledger.data.mapper.toFirestoreModelList
import com.miassolutions.milkledger.data.remote.FirestoreSyncHelper
import com.miassolutions.milkledger.domain.model.Customer
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CustomerRepository @Inject constructor(
    private val dao: CustomerDao,
    private val firestore: FirestoreSyncHelper
) {

    companion object {
        private const val COLLECTION = "customers"
        private const val TAG = "CustomerRepository"
    }

    // ----------------------------------------------------------
    // READ OPERATIONS
    // ----------------------------------------------------------

    fun getAllCustomers(): Flow<List<CustomerEntity>> = dao.getAllCustomers()

    suspend fun getCustomerById(id: String): CustomerEntity? = dao.getCustomerByIdOnce(id)

    // ----------------------------------------------------------
    // WRITE OPERATIONS + FIRESTORE SYNC
    // ----------------------------------------------------------

    suspend fun insertCustomer(customer: CustomerEntity) {
        dao.upsert(customer)

        try {
            firestore.uploadSingle(
                collectionName = COLLECTION,
                documentId = customer.customerId,
                data = customer.toFirestoreModel()
            )
        } catch (e: Exception) {
            Log.e(TAG, "Sync failed for insert: ${customer.customerId}", e)
        }
    }

    suspend fun insertAll(customers: List<CustomerEntity>) {
        dao.upsertAll(customers)

//        try {
//            firestore.uploadCollection(
//                collectionName = COLLECTION,
//                dataList = customers.toFirestoreModelList(),
//                idExtractor = { it.customerId }
//            )
//        } catch (e: Exception) {
//            Log.e(TAG, "Batch sync failed", e)
//        }
    }

    suspend fun updateCustomer(customer: CustomerEntity) {
        dao.upsert(customer)

        try {
            firestore.uploadSingle(
                collectionName = COLLECTION,
                documentId = customer.customerId,
                data = customer.toFirestoreModel()
            )
        } catch (e: Exception) {
            Log.e(TAG, "Sync failed for update: ${customer.customerId}", e)
        }
    }

    suspend fun deleteCustomer(customer: CustomerEntity) {
        dao.deleteCustomer(customer)

        try {
            firestore.deleteDocument(
                collectionName = COLLECTION,
                documentId = customer.customerId
            )
        } catch (e: Exception) {
            Log.e(TAG, "Sync failed for delete: ${customer.customerId}", e)
        }
    }

    // ----------------------------------------------------------
    // FULL SYNC / RESTORE
    // ----------------------------------------------------------

    /**
     * Restore all customers from Firestore (e.g., on reinstall).
     */
    suspend fun restoreAllCustomers() {
        Log.d(TAG, "Restoring customers from Firestore...")
        try {
            val remoteCustomers = firestore.downloadCollection<CustomerEntity>(COLLECTION)
            if (remoteCustomers.isNotEmpty()) {
                dao.upsertAll(remoteCustomers)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to restore customers", e)
        }
    }

    /**
     * Full two-way sync (optional, for periodic backup)
     */
    suspend fun synchronizeCustomers() {
        Log.d(TAG, "Starting full sync for customers...")

        try {
            // 1️⃣ Download remote
            val remoteCustomers = firestore.downloadCollection<CustomerEntity>(COLLECTION)
            if (remoteCustomers.isNotEmpty()) {
                dao.upsertAll(remoteCustomers)
            }

            // 2️⃣ Upload local
            val localCustomers = dao.getAllCustomersList()
//            firestore.uploadCollection(
//                collectionName = COLLECTION,
//                dataList = localCustomers.toFirestoreModelList(),
//                idExtractor = { it.customerId }
//            )

        } catch (e: Exception) {
            Log.e(TAG, "Full sync failed", e)
        }
    }
}

package com.miassolutions.milkledger.data.repository

import android.util.Log
import com.miassolutions.milkledger.data.local.daos.CustomerDao
import com.miassolutions.milkledger.data.local.daos.TransactionDao
import com.miassolutions.milkledger.data.local.entities.CustomerEntity
import com.miassolutions.milkledger.data.mapper.toDomain
import com.miassolutions.milkledger.data.mapper.toEntity
import com.miassolutions.milkledger.data.oldmapper.toFirestoreModel
import com.miassolutions.milkledger.data.remote.FirestoreSyncHelper
import com.miassolutions.milkledger.domain.model.Customer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
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
    // READ
    // ----------------------------------------------------------

    fun getAllCustomers(): Flow<List<Customer>> =
        dao.getAllCustomers()
            .map { list -> list.map { it.toDomain() } }

    suspend fun getCustomerById(id: String): Customer? =
        dao.getCustomerByIdOnce(id)?.toDomain()

    // ----------------------------------------------------------
    // WRITE
    // ----------------------------------------------------------

    suspend fun upsertCustomer(customer: Customer) {
        val entity = customer.toEntity()

        dao.upsert(entity)

        syncSafely {
            firestore.uploadSingle(
                collectionName = COLLECTION,
                documentId = entity.customerId,
                data = entity.toFirestoreModel()
            )
        }
    }

    suspend fun upsertCustomers(customers: List<Customer>) {
        val entities = customers.map { it.toEntity() }
        dao.upsertAll(entities)
    }

    suspend fun deleteCustomer(customerId: String) {
        val deletedAt = System.currentTimeMillis()

        dao.softDeleteById(customerId, deletedAt)

        syncSafely {
            firestore.deleteDocument(
                collectionName = COLLECTION,
                documentId = customerId
            )
        }
    }

    // ----------------------------------------------------------
    // SYNC
    // ----------------------------------------------------------

    suspend fun restoreAllCustomers() {
        try {
            val remote = firestore.downloadCollection<CustomerEntity>(COLLECTION)
            if (remote.isNotEmpty()) {
                dao.upsertAll(remote)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Restore failed", e)
        }
    }

    suspend fun synchronizeCustomers() {
        try {
            val remote = firestore.downloadCollection<CustomerEntity>(COLLECTION)
            if (remote.isNotEmpty()) {
                dao.upsertAll(remote)
            }

            val local = dao.getAllCustomersList()
            // optional upload back
        } catch (e: Exception) {
            Log.e(TAG, "Full sync failed", e)
        }
    }

    // ----------------------------------------------------------
    // UTILS
    // ----------------------------------------------------------

    private suspend fun syncSafely(block: suspend () -> Unit) {
        try {
            block()
        } catch (e: Exception) {
            Log.e(TAG, "Firestore sync failed", e)
        }
    }
}

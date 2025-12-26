package com.miassolutions.milkledger.data.repository

import android.util.Log
import com.miassolutions.milkledger.data.local.daos.CustomerDao
import com.miassolutions.milkledger.data.local.entities.CustomerEntity
import com.miassolutions.milkledger.data.mapper.toDomain
import com.miassolutions.milkledger.data.mapper.toEntity
import com.miassolutions.milkledger.presentation.expenses.data.toDomain
import com.miassolutions.milkledger.presentation.expenses.data.toEntity
import com.miassolutions.milkledger.data.remote.FirestoreSyncHelper
import com.miassolutions.milkledger.domain.model.Customer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CustomerRepository @Inject constructor(
    private val customerDao: CustomerDao,
    private val firestore: FirestoreSyncHelper
) {

    companion object {
        private const val COLLECTION = "customers"
        private const val TAG = "CustomerRepository"
    }

    // -------------------------------
    // READ
    // -------------------------------

    fun getAllCustomers(): Flow<List<Customer>> =
        customerDao.getAllCustomers()
            .map { it.map(CustomerEntity::toDomain) }

    suspend fun getCustomerById(id: String): Customer? =
        customerDao.getCustomerByIdOnce(id)?.toDomain()

    // -------------------------------
    // WRITE
    // -------------------------------

    suspend fun upsertCustomer(customer: Customer) {
        val entity = customer.toEntity()
        customerDao.upsert(entity)

//        syncSafely {
//            firestore.uploadSingle(
//                COLLECTION,
//                entity.customerId,
//                entity.toFirestoreModel()
//            )
//        }
    }

    suspend fun deleteCustomer(customerId: String) {
        val deletedAt = System.currentTimeMillis()
        customerDao.softDeleteById(customerId, deletedAt)

        syncSafely {
            firestore.deleteDocument(COLLECTION, customerId)
        }
    }

    // -------------------------------
    // SYNC (OPTIONAL)
    // -------------------------------

//    suspend fun restoreAllCustomers() {
//        try {
//            firestore.downloadCollection<CustomerEntity>(COLLECTION)
//                .takeIf { it.isNotEmpty() }
//                ?.let(customerDao::upsertAll)
//        } catch (e: Exception) {
//            Log.e(TAG, "Restore failed", e)
//        }
//    }

    // -------------------------------
    // UTILS
    // -------------------------------

    private suspend fun syncSafely(block: suspend () -> Unit) {
        try {
            block()
        } catch (e: Exception) {
            Log.e(TAG, "Firestore sync failed", e)
        }
    }
}

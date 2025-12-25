package com.miassolutions.milkledger.data.repository


import android.util.Log
import com.miassolutions.milkledger.data.local.daos.SupplierDao
import com.miassolutions.milkledger.data.local.entities.SupplierEntity
import com.miassolutions.milkledger.data.mapper.toDomain
import com.miassolutions.milkledger.data.mapper.toEntity
import com.miassolutions.milkledger.data.remote.FirestoreSyncHelper
import com.miassolutions.milkledger.domain.model.Supplier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupplierRepository @Inject constructor(
    private val supplierDao: SupplierDao,
    private val firestore: FirestoreSyncHelper
) {

    companion object {
        private const val TAG = "SupplierRepository"
        private const val COLLECTION = "suppliers"
    }

    // -------------------------------
    // READ
    // -------------------------------

    fun getAllSuppliers(): Flow<List<Supplier>> =
        supplierDao.getAllSuppliers()
            .map { it.map(SupplierEntity::toDomain) }

    fun getSupplierById(id: String): Flow<Supplier?> =
        supplierDao.getSupplierById(id)
            .map { it?.toDomain() }

    // -------------------------------
    // WRITE
    // -------------------------------

    suspend fun upsertSupplier(supplier: Supplier) {
        val entity = supplier.toEntity()
        supplierDao.insertSupplier(entity)

//        syncSafely {
//            firestore.uploadSingle(
//                COLLECTION,
//                entity.supplierId,
//                entity.toFirestoreModel()
//            )
//        }
    }

    suspend fun deleteSupplier(supplierId: String) {
        val deletedAt = System.currentTimeMillis()
        supplierDao.softDeleteById(supplierId, deletedAt)

        syncSafely {
            firestore.deleteDocument(COLLECTION, supplierId)
        }
    }

    // -------------------------------
    // SYNC (OPTIONAL)
    // -------------------------------

//    suspend fun restoreAllSuppliers() {
//        try {
//            firestore.downloadCollection<SupplierEntity>(COLLECTION)
//                .takeIf { it.isNotEmpty() }
//                ?.let(supplierDao::upsertAll)
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

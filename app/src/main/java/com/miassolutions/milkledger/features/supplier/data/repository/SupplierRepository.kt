package com.miassolutions.milkledger.features.supplier.data.repository


import com.miassolutions.milkledger.features.supplier.data.local.SupplierDao
import com.miassolutions.milkledger.features.supplier.data.local.SupplierEntity
import com.miassolutions.milkledger.features.supplier.domain.Supplier
import com.miassolutions.milkledger.features.supplier.toDomain
import com.miassolutions.milkledger.features.supplier.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupplierRepository @Inject constructor(
    private val supplierDao: SupplierDao,

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

        val existingCount = supplierDao.countWithSortOrder(supplier.sortOrder, supplier.id)

        if (existingCount > 0){
            throw SupplierSaveError.SortOrderAlreadyExists(supplier.sortOrder)
        }


        val entity = supplier.toEntity()
        supplierDao.insertSupplier(entity)


    }

    suspend fun deleteSupplier(supplierId: String) {
        val deletedAt = System.currentTimeMillis()
        supplierDao.softDeleteById(supplierId, deletedAt)


    }


}
package com.miassolutions.milkledger.data.repository


import com.miassolutions.milkledger.data.local.daos.SupplierDao
import com.miassolutions.milkledger.data.local.entities.SupplierEntity
import com.miassolutions.milkledger.data.mapper.toDomain
import com.miassolutions.milkledger.data.mapper.toEntity
import com.miassolutions.milkledger.data.util.SupplierSaveError
import com.miassolutions.milkledger.domain.model.Supplier
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

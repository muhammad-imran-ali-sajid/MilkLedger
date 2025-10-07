package com.miassolutions.milkledger.data.repository


import com.miassolutions.milkledger.data.local.SupplierDao
import com.miassolutions.milkledger.data.local.SupplierEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SupplierRepository @Inject constructor(
    private val supplierDao: SupplierDao
) {
    // Insert or replace a supplier
    suspend fun insertSupplier(supplier: SupplierEntity) {
        supplierDao.insertSupplier(supplier)
    }

    // Update an existing supplier
    suspend fun updateSupplier(supplier: SupplierEntity) {
        supplierDao.updateSupplier(supplier)
    }

    // Delete a supplier
    suspend fun deleteSupplier(supplier: SupplierEntity) {
        supplierDao.deleteSupplier(supplier)
    }

    // Get all suppliers as a Flow (live updates)
    fun getAllSuppliers(): Flow<List<SupplierEntity>> {
        return supplierDao.getAllSuppliers()
    }

    // Get a specific supplier by ID
    fun getSupplierById(id: String): Flow<SupplierEntity?> {
        return supplierDao.getSupplierById(id)
    }
}
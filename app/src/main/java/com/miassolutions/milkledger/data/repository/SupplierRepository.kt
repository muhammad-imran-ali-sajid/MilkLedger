package com.miassolutions.milkledger.data.repository


import com.miassolutions.milkledger.data.local.SupplierDao
import com.miassolutions.milkledger.data.local.SupplierEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SupplierRepository @Inject constructor(
    private val supplierDao: SupplierDao
) {
    fun getAllCustomers(): Flow<List<SupplierEntity>> = supplierDao.getAllSuppliers()

    suspend fun insertCustomer(supplier: SupplierEntity) = supplierDao.insertSupplier(supplier)
}
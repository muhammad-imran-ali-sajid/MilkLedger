package com.miassolutions.milkledger.data.repository

import com.miassolutions.milkledger.data.local.daos.CustomerDao
import com.miassolutions.milkledger.data.local.entities.CustomerEntity
import com.miassolutions.milkledger.data.mapper.toDomain
import com.miassolutions.milkledger.data.mapper.toEntity
import com.miassolutions.milkledger.data.util.CustomerSaveError
import com.miassolutions.milkledger.domain.model.Customer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CustomerRepository @Inject constructor(
    private val customerDao: CustomerDao,
) {

    fun getAllCustomers(): Flow<List<Customer>> =
        customerDao.getAllCustomers()
            .map { it.map(CustomerEntity::toDomain) }

    fun getCustomerById(id: String): Flow<Customer?> =
        customerDao.getCustomerById(id).map { it?.toDomain() }

    // -------------------------------
    // WRITE
    // -------------------------------

    suspend fun upsertCustomer(customer: Customer) {

        val existingCount = customerDao.countWithSortOrder(customer.sortOrder, customer.id)
        if (existingCount > 0) {
            throw CustomerSaveError.SortOrderAlreadyExists(customer.sortOrder)
        }

        val old = customerDao.getCustomerByIdOnce(customer.id)
        val entity = customer.toEntity().copy(
            createdAtMillis = old?.createdAtMillis ?: System.currentTimeMillis(),
            updatedAtMillis = System.currentTimeMillis()
        )


        customerDao.upsert(entity)


    }

    suspend fun deleteCustomer(customerId: String) {
        val deletedAt = System.currentTimeMillis()
        customerDao.softDeleteById(customerId, deletedAt)

    }





}

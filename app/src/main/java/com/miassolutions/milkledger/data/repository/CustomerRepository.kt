package com.miassolutions.milkledger.data.repository


import com.miassolutions.milkledger.data.local.CustomerDao
import com.miassolutions.milkledger.data.local.CustomerEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CustomerRepository @Inject constructor(
    private val customerDao: CustomerDao
) {
    // Insert or replace a customer
    suspend fun insertCustomer(customer: CustomerEntity) {
        customerDao.insertCustomer(customer)
    }

    // Update an existing customer
    suspend fun updateCustomer(customer: CustomerEntity) {
        customerDao.updateCustomer(customer)
    }

    // Delete a customer
    suspend fun deleteCustomer(customer: CustomerEntity) {
        customerDao.deleteCustomer(customer)
    }

    // Get all customers, ordered by name
    fun getAllCustomers(): Flow<List<CustomerEntity>> {
        return customerDao.getAllCustomers()
    }

    // Get a specific customer by ID
    fun getCustomerById(id: String): Flow<CustomerEntity?> {
        return customerDao.getCustomerById(id)
    }
}
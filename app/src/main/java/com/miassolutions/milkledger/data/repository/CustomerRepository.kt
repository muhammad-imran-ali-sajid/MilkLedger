package com.miassolutions.milkledger.data.repository


import com.miassolutions.milkledger.data.local.CustomerDao
import com.miassolutions.milkledger.data.local.CustomerEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CustomerRepository @Inject constructor(
    private val customerDao: CustomerDao
) {
    fun getAllCustomers(): Flow<List<CustomerEntity>> = customerDao.getAllCustomers()

    suspend fun insertCustomer(customer: CustomerEntity) = customerDao.insertCustomer(customer)
}
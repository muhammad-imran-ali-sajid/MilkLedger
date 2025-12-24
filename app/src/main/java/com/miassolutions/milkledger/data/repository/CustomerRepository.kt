package com.miassolutions.milkledger.data.repository

import android.util.Log
import com.google.firebase.firestore.Source
import com.miassolutions.milkledger.data.local.daos.CustomerDao
import com.miassolutions.milkledger.data.local.entities.CustomerEntity
import com.miassolutions.milkledger.data.remote.FirestoreSyncHelper
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CustomerRepository @Inject constructor(private val customerDao: CustomerDao) {
    suspend fun insertCustomer(customer: CustomerEntity) {
        customerDao.insertCustomer(customer)
    }

    suspend fun updateCustomer(customer: CustomerEntity) {
        customerDao.updateCustomer(customer)
    }


    suspend fun deleteCustomer(customer: CustomerEntity) {
        customerDao.deleteCustomer(customer)
    }

    fun getAllCustomers(): Flow<List<CustomerEntity>> = customerDao.getAllCustomers()


    fun getCustomerById(id: String): Flow<CustomerEntity?> = customerDao.getCustomerById(id)


}
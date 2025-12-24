package com.miassolutions.milkledger.data.local.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.miassolutions.milkledger.data.local.entities.CustomerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerDao {

    @Query("SELECT * FROM customer_table")
    suspend fun getAllCustomersList(): List<CustomerEntity>

    @Query("SELECT customerRate FROM customer_table WHERE customerId = :id")
    fun observeCustomerRate(id: String): Flow<Double>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(customers: List<CustomerEntity>)


    @Query("DELETE FROM customer_table")
    suspend fun clearAll()

    @Query("DELETE FROM customer_table WHERE customerId = :docId")
    suspend fun deleteById(docId: String)

    @Upsert
    suspend fun upsert(customer: CustomerEntity)

    @Update
    suspend fun updateCustomer(customer: CustomerEntity)

    @Delete
    suspend fun deleteCustomer(customer: CustomerEntity)

    @Query("SELECT * FROM customer_table ORDER BY sortOrder ASC")
    fun getAllCustomers(): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customer_table WHERE customerId = :id LIMIT 1")
    fun getCustomerById(id: String): Flow<CustomerEntity?>

    @Query("SELECT * FROM customer_table WHERE customerId = :id LIMIT 1")
    suspend fun getCustomerByIdOnce(id: String): CustomerEntity?
}
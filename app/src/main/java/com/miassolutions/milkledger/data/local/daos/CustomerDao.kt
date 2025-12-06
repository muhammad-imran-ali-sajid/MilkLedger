package com.miassolutions.milkledger.data.local.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.miassolutions.milkledger.data.local.entities.CustomerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerDao {

    // --- Synchronization Helper Methods ---

    /**
     * Used by the repository to get a list of all local entities for remote upload.
     */
    @Query("SELECT * FROM customer_table")
    suspend fun getAllCustomersList(): List<CustomerEntity>

    @Query("SELECT customerRate FROM customer_table WHERE customerId = :id")
    fun observeCustomerRate(id: String): Flow<Double>


    /**
     * Batch upsert (Insert or Replace) used for merging remote data into the local database.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(customers: List<CustomerEntity>)

    /**
     * Deletes all customer records.
     */
    @Query("DELETE FROM customer_table")
    suspend fun clearAll()

    @Query("DELETE FROM customer_table WHERE customerId = :docId")
    suspend fun deleteById(docId: String)


    // --- Single Entity Operations (Trigger Remote Sync) ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: CustomerEntity)

    @Update
    suspend fun updateCustomer(customer: CustomerEntity)

    @Delete
    suspend fun deleteCustomer(customer: CustomerEntity)

    // --- Local Read Operations (Offline-First Read) ---

    @Query("SELECT * FROM customer_table ORDER BY sortOrder ASC")
    fun getAllCustomers(): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customer_table WHERE customerId = :id LIMIT 1")
    fun getCustomerById(id: String): Flow<CustomerEntity?>

    @Query("SELECT * FROM customer_table WHERE customerId = :id LIMIT 1")
    suspend fun getCustomerByIdOnce(id: String): CustomerEntity?
}
package com.miassolutions.milkledger.features.customer.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerDao {

@Query("""
    SELECT COUNT(*)
    FROM customer_table
    WHERE sortOrder = :sortOder
    AND (:excludeId IS NULL OR customerId != :excludeId)
    AND deletedAtMillis IS NULL
""")
    suspend fun countWithSortOrder(sortOder: Int, excludeId: String? = null): Int

    @Query("SELECT * FROM customer_table")
    suspend fun getAllCustomersList(): List<CustomerEntity>

    @Query("""
        SELECT customerRate 
        FROM customer_table 
        WHERE customerId = :id 
          AND deletedAtMillis IS NULL
    """)
    fun observeCustomerRate(id: String): Flow<Double>

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun upsertAll(customers: List<CustomerEntity>)

    @Query("DELETE FROM customer_table")
    suspend fun clearAll()

    @Query("DELETE FROM customer_table WHERE customerId = :id")
    suspend fun deleteById(id: String)

    @Upsert
    suspend fun upsert(customer: CustomerEntity)

    @Query("""
        UPDATE customer_table 
        SET deletedAtMillis = :deletedAtMillis 
        WHERE customerId = :id
    """)
    suspend fun softDeleteById(id: String, deletedAtMillis: Long)

    @Query("""
        SELECT * FROM customer_table 
        WHERE deletedAtMillis IS NULL 
        ORDER BY sortOrder ASC
    """)
    fun getAllCustomers(): Flow<List<CustomerEntity>>

    @Query("""
        SELECT * FROM customer_table 
        WHERE customerId = :id 
          AND deletedAtMillis IS NULL
        LIMIT 1
    """)
    fun getCustomerById(id: String): Flow<CustomerEntity?>

    @Query("""
        SELECT * FROM customer_table 
        WHERE customerId = :id 
          AND deletedAtMillis IS NULL
        LIMIT 1
    """)
    suspend fun getCustomerByIdOnce(id: String): CustomerEntity?
}
package com.miassolutions.milkledger.data.local.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.miassolutions.milkledger.data.local.entities.SupplierEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SupplierDao {




    // --- Synchronization Helper Methods ---

    /**
     * Used by the repository to get a list of all local entities for remote upload.
     */
    @Query("SELECT * FROM supplier_table")
    suspend fun getAllSuppliersList(): List<SupplierEntity>

    /**
     * Batch upsert (Insert or Replace) used for merging remote data into the local database.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(suppliers: List<SupplierEntity>)

    /**
     * Deletes all supplier records.
     */
    @Query("DELETE FROM supplier_table")
    suspend fun clearAll()

    // --- Single Entity Operations (Trigger Remote Sync) ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupplier(supplier: SupplierEntity)

    @Update
    suspend fun updateSupplier(supplier: SupplierEntity)

    @Delete
    suspend fun deleteSupplier(supplier: SupplierEntity)

    /**
     * Deletes a supplier by its unique document ID.
     */
    @Query("DELETE FROM supplier_table WHERE supplierId = :docId")
    suspend fun deleteById(docId: String)

    // --- Local Read Operations (Offline-First Read) ---

    @Query("SELECT * FROM supplier_table ORDER BY sortOrder ASC")
    fun getAllSuppliers(): Flow<List<SupplierEntity>>

    @Query("SELECT * FROM supplier_table WHERE supplierId = :id LIMIT 1")
    fun getSupplierById(id: String): Flow<SupplierEntity?>
}
package com.miassolutions.milkledger.data.local.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.miassolutions.milkledger.data.local.entities.CustomerEntity
import com.miassolutions.milkledger.data.local.entities.SupplierEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SupplierDao {

    @Query("SELECT * FROM supplier_table")
    suspend fun getAllSync(): List<SupplierEntity>

    @Query("DELETE FROM supplier_table")
   suspend fun clearAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
   suspend fun insertAll(suppliers: List<SupplierEntity>)

    @Query("DELETE FROM supplier_table")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupplier(supplier: SupplierEntity)

    @Update
    suspend fun updateSupplier(supplier: SupplierEntity)

    @Delete
    suspend fun deleteSupplier(supplier: SupplierEntity)

    @Query("SELECT * FROM supplier_table ORDER BY sortOrder ASC")
    fun getAllSuppliers(): Flow<List<SupplierEntity>>

    @Query("SELECT * FROM supplier_table WHERE supplierId = :id LIMIT 1")
    fun getSupplierById(id: String): Flow<SupplierEntity?>
}
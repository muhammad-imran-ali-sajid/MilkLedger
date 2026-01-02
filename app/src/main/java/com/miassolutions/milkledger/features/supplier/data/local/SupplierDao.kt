package com.miassolutions.milkledger.features.supplier.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SupplierDao {

    /* ---------------------------------------------------
       Sync helpers
    --------------------------------------------------- */

    @Query("""
    SELECT COUNT(*)
    FROM supplier_table
    WHERE sortOrder = :sortOder
    AND (:excludeId IS NULL OR supplierId != :excludeId)
    AND deletedAtMillis IS NULL
""")
    suspend fun countWithSortOrder(sortOder: Int, excludeId: String? = null): Int

    @Query("SELECT * FROM supplier_table")
    suspend fun getAllSuppliersList(): List<SupplierEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(suppliers: List<SupplierEntity>)

    @Query("DELETE FROM supplier_table")
    suspend fun clearAll()

    /* ---------------------------------------------------
       Single entity ops
    --------------------------------------------------- */

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupplier(supplier: SupplierEntity)

    @Update
    suspend fun updateSupplier(supplier: SupplierEntity)

    @Query("""
        UPDATE supplier_table
        SET deletedAtMillis = :deletedAtMillis
        WHERE supplierId = :id
    """)
    suspend fun softDeleteById(
        id: String,
        deletedAtMillis: Long
    )

    /* ---------------------------------------------------
       Reads (offline-first)
    --------------------------------------------------- */

    @Query("""
        SELECT * FROM supplier_table
        WHERE deletedAtMillis IS NULL
        ORDER BY sortOrder ASC
    """)
    fun getAllSuppliers(): Flow<List<SupplierEntity>>

    @Query("""
        SELECT * FROM supplier_table
        WHERE supplierId = :id
          AND deletedAtMillis IS NULL
        LIMIT 1
    """)
    fun getSupplierById(id: String): Flow<SupplierEntity?>
}

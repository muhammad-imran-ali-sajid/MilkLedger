package com.miassolutions.milkledger.data.local.daos

import androidx.lifecycle.LiveData
import androidx.room.*
import com.miassolutions.milkledger.data.local.entities.SupplierEntity
import com.miassolutions.milkledger.data.local.entities.PurchaseEntity
import com.miassolutions.milkledger.data.local.relations.PurchaseWithSupplier
import com.miassolutions.milkledger.presentation.supplier.BalanceHistory
import com.miassolutions.milkledger.presentation.supplier.BalanceHistoryAdapter
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface PurchaseDao {

    // --- Synchronization Helper Methods ---

    /**
     * Used by the repository to get a list of all local entities for remote upload.
     */
    @Query("SELECT * FROM purchase_table")
    suspend fun getAllPurchasesList(): List<PurchaseEntity>



    /**
     * Batch upsert (Insert or Replace) used for merging remote data into the local database.
     */
    @Upsert
    suspend fun upsertAll(purchases: List<PurchaseEntity>)

    /**
     * Deletes all purchase records.
     */
    @Query("DELETE FROM purchase_table")
    suspend fun clearAll()

    /**
     * Deletes all purchase records associated with a specific supplier ID.
     */
    @Query("DELETE FROM purchase_table WHERE supplierId = :supplierId")
    suspend fun deleteAllBySupplierId(supplierId: String)

    // --- Single Entity Operations (Trigger Remote Sync) ---

    // ✅ Insert or replace for auto-save
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchase(purchase: PurchaseEntity)

    // ✅ Update existing entry when user changes fat, lr, volume, or notes
    @Update
    suspend fun updatePurchase(purchase: PurchaseEntity)

    // ✅ Delete specific purchase
    @Query("DELETE FROM purchase_table WHERE purchaseId = :id")
    suspend fun deletePurchase(id: String)

    // --- Local Read Operations (Offline-First Read) ---

    @Query("SELECT * FROM supplier_table")
    fun getAllSuppliers(): Flow<List<SupplierEntity>>





    // ✅ Get only the date and balance for a supplier (for a simple ledger/summary)
    @Query("SELECT date, balance FROM purchase_table WHERE supplierId = :supplierId ORDER BY date DESC")
    fun getSupplierBalanceHistory(supplierId: String): Flow<List<BalanceHistory>>

    @Transaction
    @Query("SELECT * FROM purchase_table WHERE date = :date")
    suspend fun getPurchasesByDateOnce(date: LocalDate): List<PurchaseWithSupplier>

    // ✅ Get all entries with supplier info — for reports or admin view
    @Transaction
    @Query("SELECT * FROM purchase_table ORDER BY date DESC")
    fun getAllPurchasesWithSuppliers(): Flow<List<PurchaseWithSupplier>>



    // ✅ Daily entries view (for your current screen)
    @Transaction
    @Query("SELECT * FROM purchase_table WHERE date = :date ORDER BY supplierId")
    fun getPurchasesByDate(date: LocalDate): Flow<List<PurchaseWithSupplier>>



    // ✅ Supplier ledger (date-wise history)
    @Transaction
    @Query("SELECT * FROM purchase_table WHERE supplierId = :supplierId ORDER BY date DESC")
    fun getPurchasesForSupplier(supplierId: String): Flow<List<PurchaseWithSupplier>>

    @Query("SELECT SUM(milkPrice) FROM purchase_table WHERE date BETWEEN :start AND :end")
    suspend fun getPurchasesTotalBetween(start: LocalDate, end: LocalDate): Double?
}
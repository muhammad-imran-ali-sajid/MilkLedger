package com.miassolutions.milkledger.data.local.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import androidx.room.Upsert
import com.miassolutions.milkledger.data.local.entities.PurchaseEntity
import com.miassolutions.milkledger.data.local.entities.SupplierEntity
import com.miassolutions.milkledger.data.local.relations.PurchaseWithSupplier
import com.miassolutions.milkledger.presentation.stats.SupplierPaidSummary
import com.miassolutions.milkledger.presentation.supplier.BalanceHistory
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface PurchaseDao {

    /**
     * Retrieves a list of supplier names and the amount paid to them on a specific date.
     *
     * @param targetDate The specific date (e.g., LocalDate.of(2025, 11, 17))
     */
    @Query("""
        SELECT
            T2.supplierName,
            T1.payment AS paidAmount  -- Select the amount paid from the Purchase table
        FROM
            purchase_table AS T1
        LEFT JOIN
            supplier_table AS T2
        ON
            T1.supplierId = T2.supplierId
        WHERE
            T1.date = :targetDate  -- Filter by the specific date
            AND T1.payment > 0     -- Only include purchase records where some payment was made
            AND T1.deletedAt IS NULL
        ORDER BY
            T2.supplierName ASC
    """)
    fun getPaidAmountToSupplierForDate(targetDate: LocalDate): Flow<List<SupplierPaidSummary>>

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
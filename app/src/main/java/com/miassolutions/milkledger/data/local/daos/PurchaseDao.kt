package com.miassolutions.milkledger.data.local.daos

import androidx.room.*
import com.miassolutions.milkledger.data.local.entities.CustomerEntity
import com.miassolutions.milkledger.data.local.entities.ExpensesEntity
import com.miassolutions.milkledger.data.local.entities.PurchaseEntity

import com.miassolutions.milkledger.data.local.entities.SupplierEntity
import com.miassolutions.milkledger.data.local.relations.PurchaseWithSupplier
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface PurchaseDao {

    @Query("SELECT * FROM purchase_table")
    fun getAllSync(): List<PurchaseEntity>

    @Query("DELETE FROM purchase_table")
    fun clearAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(expenses: List<PurchaseEntity>)

    @Query("SELECT * FROM supplier_table")
    fun getAllSuppliers(): Flow<List<SupplierEntity>>

    @Transaction
    @Query("SELECT * FROM purchase_table WHERE date = :date")
    suspend fun getPurchasesByDateOnce(date: LocalDate): List<PurchaseWithSupplier>

    // ✅ Insert or replace for auto-save
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchase(purchase: PurchaseEntity)

    // ✅ Update existing entry when user changes fat, lr, volume, or notes
    @Update
    suspend fun updatePurchase(purchase: PurchaseEntity)

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

    // ✅ Delete specific purchase
    @Query("DELETE FROM purchase_table WHERE purchaseId = :id")
    suspend fun deletePurchase(id: String)

    @Query("SELECT SUM(milkPrice) FROM purchase_table WHERE date BETWEEN :start AND :end")
    suspend fun getPurchasesTotalBetween(start: LocalDate, end: LocalDate): Double?
}

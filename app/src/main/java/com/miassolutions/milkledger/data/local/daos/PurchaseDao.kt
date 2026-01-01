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
import com.miassolutions.milkledger.presentation.supplier.balancehistory.BalanceHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface PurchaseDao {
//
//    // ------------------------------------------------
//    // 1️⃣ WRITE
//    // ------------------------------------------------
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    suspend fun insertPurchase(purchase: PurchaseEntity)
//
//    @Update
//    suspend fun updatePurchase(purchase: PurchaseEntity)
//
//    @Query("UPDATE purchase_table SET deletedAtMillis = :deletedAt, isSynced = 0 WHERE purchaseId = :id")
//    suspend fun softDeletePurchase(id: String, deletedAt: Long)
//
//    // ------------------------------------------------
//    // 2️⃣ READ LISTS
//    // ------------------------------------------------
//
//    // Daily Inward (Aaj kitna doodh aaya)
//    @Query("""
//        SELECT * FROM purchase_table
//        WHERE dateMillis BETWEEN :start AND :end
//        AND deletedAtMillis IS NULL
//        ORDER BY createdAtMillis DESC
//    """)
//    fun getPurchasesByDateRange(start: Long, end: Long): Flow<List<PurchaseEntity>>
//
//    // Supplier History
//    @Query("""
//        SELECT * FROM purchase_table
//        WHERE supplierId = :supplierId
//        AND deletedAtMillis IS NULL
//        ORDER BY dateMillis DESC
//    """)
//    fun getPurchasesForSupplier(supplierId: String): Flow<List<PurchaseEntity>>
//
//    @Query("SELECT * FROM purchase_table WHERE purchaseId = :id")
//    suspend fun getPurchaseById(id: String): PurchaseEntity?
//
//    // ------------------------------------------------
//    // 3️⃣ REPORTS (Stock & Quality)
//    // ------------------------------------------------
//
//    // Total Purchased Milk (Liters)
//    @Query("""
//        SELECT SUM(milkAmount) FROM purchase_table
//        WHERE dateMillis BETWEEN :start AND :end
//        AND deletedAtMillis IS NULL
//    """)
//    fun getTotalMilkPurchasedVolume(start: Long, end: Long): Flow<Double?>
//
//    // Note: Average Fat/LR hum Repository mein calculate karenge (Weighted Average logic)
//    // isliye yahan sirf raw list lene ka function kaafi hai.
//
//    // ------------------------------------------------
//    // 4️⃣ SYNC
//    // ------------------------------------------------
//    @Query("SELECT * FROM purchase_table WHERE isSynced = 0")
//    suspend fun getUnsyncedPurchases(): List<PurchaseEntity>
//
//    @Query("UPDATE purchase_table SET isSynced = 1 WHERE purchaseId = :id")
//    suspend fun markAsSynced(id: String)

    /* ---------------------------------------------------
       Aggregates
    --------------------------------------------------- */

    @Query("SELECT COUNT(*) FROM sales_table WHERE saleId = :saleId")
    suspend fun countSaleById(saleId: String): Int
    @Query("SELECT COUNT(*) FROM purchase_table WHERE purchaseId = :purchaseId")
    suspend fun countPurchaseById(purchaseId: String) : Int

    @Query("""
        SELECT IFNULL(SUM(payment), 0) 
        FROM purchase_table
        WHERE deletedAtMillis IS NULL
    """)
    fun observePurchases(): Flow<Double>

    @Query("""
        SELECT COUNT(*) 
        FROM purchase_table
        WHERE supplierId = :supplierId
          AND dateMillis = :dateMillis
          AND deletedAtMillis IS NULL
    """)
    suspend fun countPurchaseForDate(
        supplierId: String,
        dateMillis: Long
    ): Int

    /* ---------------------------------------------------
       Suppliers (read-only helpers)
    --------------------------------------------------- */

    @Query("""
        SELECT * 
        FROM supplier_table
        WHERE deletedAtMillis IS NULL
        ORDER BY sortOrder ASC
    """)
    fun observeSuppliersList(): Flow<List<SupplierEntity>>

    /* ---------------------------------------------------
       Paid amount summary per supplier (daily)
    --------------------------------------------------- */

    @Query(
        """
        SELECT
            s.supplierName AS supplierName,
            p.payment AS paidAmount,
            p.milkAmount AS volume
        FROM purchase_table p
        LEFT JOIN supplier_table s
            ON p.supplierId = s.supplierId
        WHERE p.dateMillis = :dateMillis
          AND p.payment > 0
          AND p.deletedAtMillis IS NULL
        ORDER BY s.supplierName ASC
        """
    )
    fun getPaidAmountToSupplierForDate(
        dateMillis: Long
    ): Flow<List<SupplierPaidSummary>>

    /* ---------------------------------------------------
       Sync helpers
    --------------------------------------------------- */

    @Query("SELECT * FROM purchase_table")
    suspend fun getAllPurchasesList(): List<PurchaseEntity>

    @Upsert
    suspend fun upsertAll(purchases: List<PurchaseEntity>)

    @Query("DELETE FROM purchase_table")
    suspend fun clearAll()

    @Query("""
        UPDATE purchase_table
        SET deletedAtMillis = :deletedAtMillis
        WHERE supplierId = :supplierId
    """)
    suspend fun softDeleteAllBySupplierId(
        supplierId: String,
        deletedAtMillis: Long
    )

    /* ---------------------------------------------------
       Single entity ops
    --------------------------------------------------- */

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchase(purchase: PurchaseEntity)

    @Update
    suspend fun updatePurchase(purchase: PurchaseEntity)

    @Query("""
        UPDATE purchase_table
        SET deletedAtMillis = :deletedAtMillis
        WHERE purchaseId = :id
    """)
    suspend fun softDeletePurchase(
        id: String,
        deletedAtMillis: Long
    )

    /* ---------------------------------------------------
       Daily stats
    --------------------------------------------------- */

    @Query("""
        SELECT AVG(fat)
        FROM purchase_table
        WHERE dateMillis = :dateMillis
          AND fat > 0
          AND deletedAtMillis IS NULL
    """)
    fun getTotalFat(dateMillis: Long): Flow<Double?>

    @Query("""
        SELECT AVG(lr)
        FROM purchase_table
        WHERE dateMillis = :dateMillis
          AND lr > 0
          AND deletedAtMillis IS NULL
    """)
    fun getTotalLr(dateMillis: Long): Flow<Double?>

    @Query("""
        SELECT SUM(ts)
        FROM purchase_table
        WHERE dateMillis = :dateMillis
          AND fat > 0
          AND lr > 0
          AND deletedAtMillis IS NULL
    """)
    fun getTotalTs(dateMillis: Long): Flow<Double?>

    @Query("""
        SELECT SUM(milkAmount)
        FROM purchase_table
        WHERE dateMillis = :dateMillis
          AND fat > 0
          AND lr > 0
          AND deletedAtMillis IS NULL
    """)
    fun getTotalMilkWithFatLR(dateMillis: Long): Flow<Double?>

    /* ---------------------------------------------------
       Supplier ledger / history
    --------------------------------------------------- */
//
//    @Query("""
//        SELECT dateMillis, balance
//        FROM purchase_table
//        WHERE supplierId = :supplierId
//          AND deletedAtMillis IS NULL
//        ORDER BY dateMillis DESC
//    """)
//    fun getSupplierBalanceHistory(
//        supplierId: String
//    ): Flow<List<BalanceHistory>>

    @Transaction
    @Query("""
        SELECT *
        FROM purchase_table
        WHERE supplierId = :supplierId
          AND deletedAtMillis IS NULL
        ORDER BY dateMillis ASC
    """)
    suspend fun getSupplierHistoryOnce(
        supplierId: String
    ): List<PurchaseWithSupplier>

    /* ---------------------------------------------------
       Date-based reads
    --------------------------------------------------- */

    @Transaction
    @Query("""
        SELECT *
        FROM purchase_table
        WHERE dateMillis = :dateMillis
          AND deletedAtMillis IS NULL
        ORDER BY supplierId
    """)
    fun getPurchasesByDate(
        dateMillis: Long
    ): Flow<List<PurchaseWithSupplier>>

    @Transaction
    @Query("""
        SELECT *
        FROM purchase_table
        WHERE dateMillis = :dateMillis
          AND deletedAtMillis IS NULL
    """)
    suspend fun getPurchasesByDateOnce(
        dateMillis: Long
    ): List<PurchaseWithSupplier>

    @Transaction
    @Query("""
        SELECT *
        FROM purchase_table
        WHERE deletedAtMillis IS NULL
        ORDER BY dateMillis DESC
    """)
    fun getAllPurchasesWithSuppliers(): Flow<List<PurchaseWithSupplier>>

    @Transaction
    @Query("""
        SELECT *
        FROM purchase_table
        WHERE supplierId = :supplierId
          AND deletedAtMillis IS NULL
        ORDER BY dateMillis DESC
    """)
    fun getPurchasesForSupplier(
        supplierId: String
    ): Flow<List<PurchaseWithSupplier>>

    /* ---------------------------------------------------
       Range totals
    --------------------------------------------------- */

}

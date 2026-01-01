package com.miassolutions.milkledger.data.local.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.miassolutions.milkledger.data.local.entities.CustomerEntity
import com.miassolutions.milkledger.data.local.entities.SalesEntity
import com.miassolutions.milkledger.data.local.relations.SaleWithCustomer
import com.miassolutions.milkledger.presentation.stats.CustomerPaidSummary
import com.miassolutions.milkledger.presentation.supplier.balancehistory.BalanceHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface SalesDao {

    // ------------------------------------------------
    // 1️⃣ WRITE (Likhna)
    // ------------------------------------------------
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSale(sale: SalesEntity)

    @Update
    suspend fun updateSale(sale: SalesEntity)

    // Soft Delete (Record chupana, delete nahi karna)
    @Query("UPDATE sales_table SET deletedAtMillis = :deletedAt, isSynced = 0 WHERE saleId = :saleId")
    suspend fun softDeleteSale(saleId: String, deletedAt: Long)

    // ------------------------------------------------
    // 2️⃣ READ LISTS (UI ke liye)
    // ------------------------------------------------

    // Daily Sheet (Aaj ke din ki saari sales)
    @Query("""
        SELECT * FROM sales_table 
        WHERE dateMillis BETWEEN :start AND :end 
        AND deletedAtMillis IS NULL 
        ORDER BY createdAtMillis DESC
    """)
    fun getSalesByDateRange(start: Long, end: Long): Flow<List<SalesEntity>>

    // Customer History (Ek banday ne kab kab doodh liya)
    @Query("""
        SELECT * FROM sales_table 
        WHERE customerId = :customerId 
        AND deletedAtMillis IS NULL 
        ORDER BY dateMillis DESC
    """)
    fun getSalesForCustomer(customerId: String): Flow<List<SalesEntity>>

    // Single Sale (Edit karne ke liye)
    @Query("SELECT * FROM sales_table WHERE saleId = :id")
    suspend fun getSaleById(id: String): SalesEntity?

    // ------------------------------------------------
    // 3️⃣ REPORTS (Doodh ka Hisab)
    // ------------------------------------------------

    // Aaj Total kitne Liter Doodh Bika?
    @Query("""
        SELECT SUM(volume) FROM sales_table 
        WHERE dateMillis BETWEEN :start AND :end 
        AND deletedAtMillis IS NULL
    """)
    fun getTotalMilkSoldVolume(start: Long, end: Long): Flow<Double?>

    // ------------------------------------------------
    // 4️⃣ SYNC (Backup)
    // ------------------------------------------------
    @Query("SELECT * FROM sales_table WHERE isSynced = 0")
    suspend fun getUnsyncedSales(): List<SalesEntity>

    @Query("UPDATE sales_table SET isSynced = 1 WHERE saleId = :id")
    suspend fun markAsSynced(id: String)

    /* ---------------------------------------------------
       Aggregates
    --------------------------------------------------- */

    @Query("""
        SELECT IFNULL(SUM(paid), 0)
        FROM sales_table
        WHERE deletedAtMillis IS NULL
    """)
    fun observeSales(): Flow<Double>

    @Query("""
        SELECT customerRate
        FROM customer_table
        WHERE customerId = :id
          AND deletedAtMillis IS NULL
    """)
    fun observeCustomerRate(id: String): Flow<Double>

    /* ---------------------------------------------------
       Customers (read-only helpers)
    --------------------------------------------------- */

    @Query("""
        SELECT *
        FROM customer_table
        WHERE deletedAtMillis IS NULL
        ORDER BY sortOrder ASC
    """)
    fun observeCustomersList(): Flow<List<CustomerEntity>>

    /* ---------------------------------------------------
       Counts / Validation
    --------------------------------------------------- */

    @Query("""
        SELECT COUNT(*)
        FROM sales_table
        WHERE customerId = :customerId
          AND dateMillis = :dateMillis
          AND deletedAtMillis IS NULL
    """)
    suspend fun countSalesForDate(
        customerId: String,
        dateMillis: Long
    ): Int

    /* ---------------------------------------------------
       Paid summary (daily)
    --------------------------------------------------- */

    @Query("""
        SELECT
            c.customerName AS customerName,
            s.paid AS paidAmount,
            s.volume AS volume
        FROM sales_table s
        LEFT JOIN customer_table c
            ON s.customerId = c.customerId
        WHERE s.dateMillis = :dateMillis
          AND s.paid > 0
          AND s.deletedAtMillis IS NULL
        ORDER BY c.customerName ASC
    """)
    fun getPaidAmountForDate(
        dateMillis: Long
    ): Flow<List<CustomerPaidSummary>>

    /* ---------------------------------------------------
       Customer balance / history
    --------------------------------------------------- */

    @Query("""
        SELECT dateMillis, balance
        FROM sales_table
        WHERE customerId = :customerId
          AND deletedAtMillis IS NULL
        ORDER BY dateMillis DESC
    """)
    suspend fun getCustomerBalanceHistory(
        customerId: String
    ): List<BalanceHistory>

    @Transaction
    @Query("""
        SELECT *
        FROM sales_table
        WHERE customerId = :customerId
          AND deletedAtMillis IS NULL
        ORDER BY dateMillis ASC
    """)
    suspend fun getCustomerBalanceHistoryOnce(
        customerId: String
    ): List<SaleWithCustomer>

    /* ---------------------------------------------------
       Sync helpers
    --------------------------------------------------- */

    @Query("""
        SELECT *
        FROM sales_table
        ORDER BY dateMillis DESC
    """)
    suspend fun getAllSalesList(): List<SalesEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(sales: List<SalesEntity>)

    @Query("DELETE FROM sales_table")
    suspend fun clearAll()

    /* ---------------------------------------------------
       Single entity ops
    --------------------------------------------------- */

//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    suspend fun insertSale(sale: SalesEntity)
//
//    @Update
//    suspend fun updateSale(sale: SalesEntity)

    @Query("SELECT COUNT(*) FROM sales_table WHERE saleId = :saleId")
    suspend fun countSaleById(saleId: String): Int

//    @Query("""
//        UPDATE sales_table
//        SET deletedAtMillis = :deletedAtMillis
//        WHERE saleId = :id
//    """)
//    suspend fun softDeleteSale(
//        id: String,
//        deletedAtMillis: Long
//    )

    @Query("""
        UPDATE sales_table
        SET deletedAtMillis = :deletedAtMillis
        WHERE customerId = :customerId
    """)
    suspend fun softDeleteAllByCustomerId(
        customerId: String,
        deletedAtMillis: Long
    )

    /* ---------------------------------------------------
       Reads (relations)
    --------------------------------------------------- */

    @Transaction
    @Query("""
        SELECT *
        FROM sales_table
        WHERE deletedAtMillis IS NULL
        ORDER BY dateMillis DESC
    """)
    fun getAllSalesWithCustomers(): Flow<List<SaleWithCustomer>>

    @Transaction
    @Query("""
        SELECT *
        FROM sales_table
        WHERE dateMillis = :dateMillis
          AND deletedAtMillis IS NULL
        ORDER BY customerId
    """)
    fun getSalesByDate(
        dateMillis: Long
    ): Flow<List<SaleWithCustomer>>

//    @Transaction
//    @Query("""
//        SELECT *
//        FROM sales_table
//        WHERE customerId = :customerId
//          AND deletedAtMillis IS NULL
//        ORDER BY dateMillis DESC
//    """)
//    fun getSalesForCustomer(
//        customerId: String
//    ): Flow<List<SaleWithCustomer>>

    @Transaction
    @Query("""
        SELECT *
        FROM sales_table
        WHERE dateMillis = :dateMillis
          AND deletedAtMillis IS NULL
    """)
    suspend fun getSalesByDateOnce(
        dateMillis: Long
    ): List<SaleWithCustomer>

    /* ---------------------------------------------------
       Range totals
    --------------------------------------------------- */

    @Query(
        """
        SELECT IFNULL(SUM(totalAmount), 0)
        FROM sales_table
        WHERE dateMillis BETWEEN :startMillis AND :endMillis
          AND deletedAtMillis IS NULL
    """
    )
    suspend fun getSalesTotalBetween(
        startMillis: Long,
        endMillis: Long
    ): Double

    /* ---------------------------------------------------
       Customer helpers
    --------------------------------------------------- */

    @Query("""
        SELECT *
        FROM customer_table
        WHERE deletedAtMillis IS NULL
        ORDER BY sortOrder ASC
    """)
    fun getAllCustomers(): Flow<List<CustomerEntity>>

    @Query("""
        SELECT *
        FROM customer_table
        WHERE deletedAtMillis IS NULL
        ORDER BY sortOrder ASC
    """)
    suspend fun getAllCustomersOnce(): List<CustomerEntity>
}

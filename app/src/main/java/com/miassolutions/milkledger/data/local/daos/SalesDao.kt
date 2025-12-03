package com.miassolutions.milkledger.data.local.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.miassolutions.milkledger.data.local.entities.CustomerEntity
import com.miassolutions.milkledger.data.local.entities.SalesEntity
import com.miassolutions.milkledger.data.local.entities.SupplierEntity
import com.miassolutions.milkledger.data.local.relations.SaleWithCustomer
import com.miassolutions.milkledger.presentation.stats.CustomerPaidSummary
import com.miassolutions.milkledger.presentation.supplier.BalanceHistory
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface SalesDao {

    @Query("SELECT IFNULL(SUM(paid), 0) FROM sales_table")
    fun observeSales(): Flow<Double>


    @Query("""
        SELECT *
        FROM customer_table
    """)
    fun observeCustomersList() : Flow<List<CustomerEntity>>


    @Query("""
    SELECT COUNT(*) FROM sales_table 
    WHERE customerId = :customerId AND date = :date
""")
    suspend fun countSalesForDate(customerId: String, date: LocalDate): Int

    /*
    * @param targetDate The specific date (e.g., LocalDate.of(2025, 11, 17))
    */
    @Query("""
        SELECT
            T2.customerName,
            T1.paid AS paidAmount,  -- Select the paid amount for that sale
            T1.volume AS volume
        FROM
            sales_table AS T1
        LEFT JOIN
            customer_table AS T2
        ON
            T1.customerId = T2.customerId
        WHERE
            T1.date = :targetDate  -- Filter by the specific date
            AND T1.paid > 0        -- Only include sales where some amount was paid
            AND T1.deletedAt IS NULL
        ORDER BY
            T2.customerName ASC
    """)
    fun getPaidAmountForDate(targetDate: LocalDate): Flow<List<CustomerPaidSummary>>


    @Query("SELECT date, balance FROM sales_table WHERE customerId = :customerId ORDER BY date DESC")
    suspend fun getCustomerBalanceHistory(customerId: String): List<BalanceHistory>

    // --- Synchronization Helper Functions (Used by Repository's synchronizeSales()) ---

    /**
     * Required for uploading all local data to Firestore during sync.
     * Renamed from getAllSync to be clearer.
     */
    @Query("SELECT * FROM sales_table ORDER BY date DESC")
    suspend fun getAllSalesList(): List<SalesEntity>

    /**
     * Required for merging downloaded remote data into the local database.
     * Uses REPLACE strategy to handle updates/inserts efficiently (the 'upsert' function).
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(sales: List<SalesEntity>)

    @Query("DELETE FROM sales_table")
    suspend fun clearAll()


    // --- CRUD Operations (Used by Repository's insert/update/deleteSale()) ---

    // 🟢 Insert or replace a single sale (used for new local entries)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSale(sale: SalesEntity)

    // 🟡 Update a single sale
    @Update
    suspend fun updateSale(sale: SalesEntity)

    // 🔴 Delete specific sale
    @Query("DELETE FROM sales_table WHERE saleId = :id")
    suspend fun deleteSale(id: String)


    /**
     * Deletes all purchase records associated with a specific supplier ID.
     */
    @Query("DELETE FROM sales_table WHERE customerId = :customerId")
    suspend fun deleteAllByCustomerId(customerId: String)

    // --- Read Operations (Purely Local Flow/One-Time Fetches) ---

    // 🧾 Get all entries with customer info — for reports or admin view
    @Transaction
    @Query("SELECT * FROM sales_table ORDER BY date DESC")
    fun getAllSalesWithCustomers(): Flow<List<SaleWithCustomer>>

    // 📅 Daily entries view (for your current screen)
    @Transaction
    @Query("SELECT * FROM sales_table WHERE date = :date ORDER BY customerId")
    fun getSalesByDate(date: LocalDate): Flow<List<SaleWithCustomer>>

    // 👤 Customer ledger (date-wise history)
    @Transaction
    @Query("SELECT * FROM sales_table WHERE customerId = :customerId ORDER BY date DESC")
    fun getSalesForCustomer(customerId: String): Flow<List<SaleWithCustomer>>

    // ✅ Get sales for a specific date once (e.g. for one-time report export)
    @Transaction
    @Query("SELECT * FROM sales_table WHERE date = :date")
    suspend fun getSalesByDateOnce(date: LocalDate): List<SaleWithCustomer>

    // 📈 Get sales total between dates
    @Query("SELECT SUM(price) FROM sales_table WHERE date BETWEEN :start AND :end")
    suspend fun getSalesTotalBetween(start: LocalDate, end: LocalDate): Double?

    // --- Customer Read Helpers (Included for convenience/relations) ---

    @Query("SELECT * FROM customer_table ORDER BY sortOrder ASC")
    fun getAllCustomers(): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customer_table ORDER BY sortOrder ASC")
    suspend fun getAllCustomersOnce(): List<CustomerEntity>
}
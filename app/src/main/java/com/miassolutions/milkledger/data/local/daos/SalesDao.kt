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
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface SalesDao {

    // --- Synchronization Helper Functions (Used by Repository's synchronizeSales()) ---

    /**
     * Required for uploading all local data to Firestore during sync.
     * Renamed from getAllSync to be clearer.
     */
    @Query("SELECT * FROM sales_table")
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
    @Query("SELECT * FROM sales_table WHERE customerId = :customerId ORDER BY date ASC")
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
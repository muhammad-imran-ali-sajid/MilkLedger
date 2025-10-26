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

    // ✅ Insert or replace for auto-save
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSale(sale: SalesEntity)

    @Query("SELECT * FROM customer_table ORDER BY sortOrder ASC")
    fun getAllCustomers(): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customer_table ORDER BY sortOrder ASC")
    suspend fun getAllCustomersOnce(): List<CustomerEntity>

    // ✅ Update sale if needed
    @Update
    suspend fun updateSale(sale: SalesEntity)

    // ✅ Delete specific sale
    @Query("DELETE FROM sales_table WHERE saleId = :id")
    suspend fun deleteSale(id: String)

    // ✅ Get all entries with customer info — for reports or admin view
    @Transaction
    @Query("SELECT * FROM sales_table ORDER BY date DESC")
    fun getAllSalesWithCustomers(): Flow<List<SaleWithCustomer>>

    // ✅ Daily entries view (for your current screen)
    @Transaction
    @Query("SELECT * FROM sales_table WHERE date = :date ORDER BY customerId")
    fun getSalesByDate(date: LocalDate): Flow<List<SaleWithCustomer>>

    // ✅ Customer ledger (date-wise history)
    @Transaction
    @Query("SELECT * FROM sales_table WHERE customerId = :customerId ORDER BY date ASC")
    fun getSalesForCustomer(customerId: String): Flow<List<SaleWithCustomer>>

    // ✅ Get sales for a specific date once (e.g. for one-time report export)
    @Transaction
    @Query("SELECT * FROM sales_table WHERE date = :date")
    suspend fun getSalesByDateOnce(date: LocalDate): List<SaleWithCustomer>


    @Query("SELECT SUM(price) FROM sales_table WHERE date BETWEEN :start AND :end")
    suspend fun getSalesTotalBetween(start: LocalDate, end: LocalDate): Double?
}

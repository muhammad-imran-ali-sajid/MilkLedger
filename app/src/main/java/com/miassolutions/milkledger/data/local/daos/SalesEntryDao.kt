package com.miassolutions.milkledger.data.local.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.miassolutions.milkledger.data.local.entities.SalesEntryEntity
import com.miassolutions.milkledger.data.local.relations.SaleWithCustomer
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface SalesEntryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSale(sale: SalesEntryEntity)

    @Transaction
    @Query("SELECT * FROM sales_entry_table ORDER BY date DESC")
    fun getAllSalesWithCustomers(): Flow<List<SaleWithCustomer>>

    @Transaction
    @Query("SELECT * FROM sales_entry_table WHERE date = :date ORDER BY customerId")
    fun getSalesByDate(date: LocalDate): Flow<List<SaleWithCustomer>>

    @Transaction
    @Query("SELECT * FROM sales_entry_table WHERE customerId = :customerId ORDER BY date DESC")
    fun getSalesForCustomer(customerId: String): Flow<List<SaleWithCustomer>>

    @Query("DELETE FROM sales_entry_table WHERE saleId = :id")
    suspend fun deleteSale(id: String)
}

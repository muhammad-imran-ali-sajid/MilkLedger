package com.miassolutions.milkledger.data.local.daos

import androidx.room.Dao
import androidx.room.Query
import com.miassolutions.milkledger.data.local.entities.PurchaseEntryEntity
import com.miassolutions.milkledger.data.local.entities.SalesEntryEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface ReportsDao {

    // --- Sales ---

    @Query("SELECT * FROM sales_entry_table WHERE customerId = :customerId ORDER BY date DESC")
    fun getCustomerSalesHistory(customerId: String): Flow<List<SalesEntryEntity>>

    @Query("SELECT SUM(netMilk) FROM sales_entry_table WHERE date = :date")
    fun getTotalMilkSoldOn(date: LocalDate): Flow<Double?>

    @Query("SELECT SUM(price) FROM sales_entry_table WHERE date BETWEEN :start AND :end")
    fun getTotalSalesBetween(start: LocalDate, end: LocalDate): Flow<Double?>


    // --- Purchases ---


    @Query("SELECT * FROM purchase_entry_table WHERE supplierId = :supplierId ORDER BY date DESC")
    fun getSupplierPurchaseHistory(supplierId: String): Flow<List<PurchaseEntryEntity>>

    @Query("SELECT SUM(milkPrice) FROM purchase_entry_table WHERE date BETWEEN :start AND :end")
    fun getTotalPurchasesBetween(start: LocalDate, end: LocalDate): Flow<Double?>


    // --- Expenses ---

    @Query("SELECT SUM(expenseAmount) FROM expense_table WHERE date BETWEEN :start AND :end")
    fun getTotalExpensesBetween(start: LocalDate, end: LocalDate): Flow<Double?>
}

package com.miassolutions.milkledger.data.local.daos

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.miassolutions.milkledger.data.local.entities.ProfitEntity
import com.miassolutions.milkledger.data.local.entities.PurchaseEntity
import com.miassolutions.milkledger.data.local.entities.SalesEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface ReportsDao {

    @Upsert
    suspend fun upsertProfit(profitEntity: ProfitEntity)
    // ───────────────────────────────
    // 🧾 SALES
    // ───────────────────────────────

    @Query("SELECT * FROM sales_table WHERE customerId = :customerId ORDER BY date DESC")
    fun getCustomerSalesHistory(customerId: String): Flow<List<SalesEntity>>

    @Query("SELECT SUM(netMilk) FROM sales_table WHERE date = :date")
    fun getTotalMilkSoldOn(date: LocalDate): Flow<Double?>

    @Query("SELECT SUM(netMilk) FROM sales_table WHERE date BETWEEN :start AND :end")
    fun getTotalMilkSoldBetween(start: LocalDate, end: LocalDate): Flow<Double?>

    @Query("SELECT AVG(fat) FROM purchase_table WHERE date BETWEEN :start AND :end AND fat > 0.0")
    fun getAvgFatBetween(start: LocalDate, end: LocalDate): Flow<Double?>

    @Query("SELECT SUM(milkAmount) FROM purchase_table WHERE date BETWEEN :start AND :end AND fat > 0.0 AND lr > 0.0")
    fun getTotalMilkWithFatAndLrBetween(start: LocalDate, end: LocalDate): Flow<Double?>

    @Query("SELECT AVG(lr) FROM purchase_table WHERE date BETWEEN :start AND :end AND fat > 0.0")
    fun getAvgLrBetween(start: LocalDate, end: LocalDate): Flow<Double?>

    @Query("SELECT SUM(ts) FROM purchase_table WHERE date BETWEEN :start AND :end")
    fun getTsBetween(start: LocalDate, end: LocalDate): Flow<Double?>

    @Query("SELECT SUM(price) FROM sales_table WHERE date BETWEEN :start AND :end")
    fun getTotalSalesBetween(start: LocalDate, end: LocalDate): Flow<Double?>


    // ➕ All Records (Sales)
    @Query("SELECT SUM(netMilk) FROM sales_table")
    fun getTotalMilkSoldAll(): Flow<Double?>

    @Query("SELECT SUM(price) FROM sales_table")
    fun getTotalSalesAll(): Flow<Double?>

    @Query("SELECT paid FROM sales_table WHERE customerId =:customerId AND paid > 0.0")
    fun getPaymentReceived(customerId: String): Double


    // ───────────────────────────────
    // 🧾 PURCHASES
    // ───────────────────────────────

    @Query("SELECT * FROM purchase_table WHERE supplierId = :supplierId ORDER BY date DESC")
    fun getSupplierPurchaseHistory(supplierId: String): Flow<List<PurchaseEntity>>

    @Query("SELECT SUM(milkAmount) FROM purchase_table WHERE date BETWEEN :start AND :end")
    fun getTotalMilkPurchaseBetween(start: LocalDate, end: LocalDate): Flow<Double?>

    @Query("SELECT SUM(milkPrice) FROM purchase_table WHERE date BETWEEN :start AND :end")
    fun getTotalPurchasesBetween(start: LocalDate, end: LocalDate): Flow<Double?>



    // ➕ All Records (Purchases)
    @Query("SELECT SUM(milkAmount) FROM purchase_table")
    fun getTotalMilkPurchaseAll(): Flow<Double?>

    @Query("SELECT AVG(fat) FROM purchase_table WHERE  fat > 0.0")
    fun getTotalFat(): Flow<Double?>

    @Query("SELECT AVG(lr) FROM purchase_table WHERE lr > 0.0")
    fun getTotalLr(): Flow<Double?>


    @Query("SELECT SUM(ts) FROM purchase_table WHERE fat > 0.0 AND lr > 0.0")
    fun getTotalTs(): Flow<Double?>

    @Query("SELECT SUM(milkPrice) FROM purchase_table")
    fun getTotalPurchasesAll(): Flow<Double?>


    // ───────────────────────────────
    // 💰 EXPENSES
    // ───────────────────────────────

    @Query("SELECT SUM(expenseAmount) FROM expense_table WHERE date BETWEEN :start AND :end")
    fun getTotalExpensesBetween(start: LocalDate, end: LocalDate): Flow<Double?>

    @Query("SELECT SUM(expenseAmount) FROM expense_table WHERE date BETWEEN :start AND :end AND isDefault =1")
    fun getTotalFixedExpensesBetween(start: LocalDate, end: LocalDate): Flow<Double?>

    @Query("SELECT SUM(expenseAmount) FROM expense_table WHERE date BETWEEN :start AND :end AND isDefault =0")
    fun getTotalPersonalExpensesBetween(start: LocalDate, end: LocalDate): Flow<Double?>

    // ➕ All Records (Expenses)
    @Query("SELECT SUM(expenseAmount) FROM expense_table WHERE isDefault = 1")
    fun getTotalFixedExpensesAll(): Flow<Double?>

    @Query("SELECT SUM(expenseAmount) FROM expense_table WHERE isDefault = 0")
    fun getTotalPersonalExpensesAll(): Flow<Double?>

    @Query("SELECT SUM(expenseAmount) FROM expense_table")
    fun getTotalExpensesAll(): Flow<Double?>




    @Query("SELECT SUM(price) FROM sales_table WHERE date = :date")
    fun getTotalSalesDaily(date: LocalDate): Flow<Double?>

    @Query("SELECT SUM(milkPrice) FROM purchase_table WHERE date = :date")
    fun getTotalPurchasesDaily(date: LocalDate): Flow<Double?>

    @Query("SELECT SUM(expenseAmount) FROM expense_table WHERE date = :date AND isDefault = 1")
    fun getTotalBusinessExpensesDaily(date: LocalDate): Flow<Double?>



    @Query("SELECT SUM(expenseAmount) FROM expense_table WHERE date = :date")
    fun getTotalExpensesDaily(date: LocalDate): Flow<Double?>







}

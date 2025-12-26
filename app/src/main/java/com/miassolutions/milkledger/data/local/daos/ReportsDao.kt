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

    /* ---------------------------------------------------
       PROFIT (derived snapshots)
    --------------------------------------------------- */

    @Upsert
    suspend fun upsertProfit(profitEntity: ProfitEntity)

    /* ---------------------------------------------------
       SALES
    --------------------------------------------------- */

    @Query("""
        SELECT *
        FROM sales_table
        WHERE customerId = :customerId
          AND deletedAtMillis IS NULL
        ORDER BY dateMillis DESC
    """)
    fun getCustomerSalesHistory(
        customerId: String
    ): Flow<List<SalesEntity>>

    @Query("""
        SELECT SUM(netMilk)
        FROM sales_table
        WHERE dateMillis = :dateMillis
          AND deletedAtMillis IS NULL
    """)
    fun getTotalMilkSoldOn(
        dateMillis: Long
    ): Flow<Double?>

    @Query("""
        SELECT SUM(netMilk)
        FROM sales_table
        WHERE dateMillis BETWEEN :startMillis AND :endMillis
          AND deletedAtMillis IS NULL
    """)
    fun getTotalMilkSoldBetween(
        startMillis: Long,
        endMillis: Long
    ): Flow<Double?>

    @Query("""
        SELECT SUM(price)
        FROM sales_table
        WHERE dateMillis BETWEEN :startMillis AND :endMillis
          AND deletedAtMillis IS NULL
    """)
    fun getTotalSalesBetween(
        startMillis: Long,
        endMillis: Long
    ): Flow<Double?>

    @Query("""
        SELECT IFNULL(SUM(netMilk), 0)
        FROM sales_table
        WHERE deletedAtMillis IS NULL
    """)
    fun getTotalMilkSoldAll(): Flow<Double>

    @Query("""
        SELECT IFNULL(SUM(price), 0)
        FROM sales_table
        WHERE deletedAtMillis IS NULL
    """)
    fun getTotalSalesAll(): Flow<Double>

    @Query("""
        SELECT IFNULL(SUM(paid), 0)
        FROM sales_table
        WHERE customerId = :customerId
          AND paid > 0
          AND deletedAtMillis IS NULL
    """)
    suspend fun getPaymentReceived(
        customerId: String
    ): Double

    @Query("""
        SELECT IFNULL(SUM(price), 0)
        FROM sales_table
        WHERE dateMillis = :dateMillis
          AND deletedAtMillis IS NULL
    """)
    fun getTotalSalesDaily(
        dateMillis: Long
    ): Flow<Double>

    /* ---------------------------------------------------
       PURCHASES
    --------------------------------------------------- */

    @Query("""
        SELECT *
        FROM purchase_table
        WHERE supplierId = :supplierId
          AND deletedAtMillis IS NULL
        ORDER BY dateMillis DESC
    """)
    fun getSupplierPurchaseHistory(
        supplierId: String
    ): Flow<List<PurchaseEntity>>

    @Query("""
        SELECT SUM(milkAmount)
        FROM purchase_table
        WHERE dateMillis BETWEEN :startMillis AND :endMillis
          AND deletedAtMillis IS NULL
    """)
    fun getTotalMilkPurchaseBetween(
        startMillis: Long,
        endMillis: Long
    ): Flow<Double?>

    @Query("""
        SELECT SUM(milkPrice)
        FROM purchase_table
        WHERE dateMillis BETWEEN :startMillis AND :endMillis
          AND deletedAtMillis IS NULL
    """)
    fun getTotalPurchasesBetween(
        startMillis: Long,
        endMillis: Long
    ): Flow<Double?>

    @Query("""
        SELECT IFNULL(SUM(milkAmount), 0)
        FROM purchase_table
        WHERE deletedAtMillis IS NULL
    """)
    fun getTotalMilkPurchaseAll(): Flow<Double>

    @Query("""
        SELECT AVG(fat)
        FROM purchase_table
        WHERE fat > 0
          AND deletedAtMillis IS NULL
    """)
    fun getTotalFat(): Flow<Double?>

    @Query("""
        SELECT AVG(lr)
        FROM purchase_table
        WHERE lr > 0
          AND deletedAtMillis IS NULL
    """)
    fun getTotalLr(): Flow<Double?>

    @Query("""
        SELECT SUM(ts)
        FROM purchase_table
        WHERE fat > 0
          AND lr > 0
          AND deletedAtMillis IS NULL
    """)
    fun getTotalTs(): Flow<Double?>

    @Query("""
        SELECT IFNULL(SUM(milkPrice), 0)
        FROM purchase_table
        WHERE deletedAtMillis IS NULL
    """)
    fun getTotalPurchasesAll(): Flow<Double>

    @Query("""
        SELECT IFNULL(SUM(milkPrice), 0)
        FROM purchase_table
        WHERE dateMillis = :dateMillis
          AND deletedAtMillis IS NULL
    """)
    fun getTotalPurchasesDaily(
        dateMillis: Long
    ): Flow<Double>

    /* ---------------------------------------------------
       EXPENSES
    --------------------------------------------------- */

    @Query("""
        SELECT SUM(expenseAmount)
        FROM expense_table
        WHERE dateMillis BETWEEN :startMillis AND :endMillis
          AND deletedAtMillis IS NULL
    """)
    fun getTotalExpensesBetween(
        startMillis: Long,
        endMillis: Long
    ): Flow<Double?>

    @Query("""
        SELECT SUM(expenseAmount)
        FROM expense_table
        WHERE dateMillis BETWEEN :startMillis AND :endMillis
          AND isBusiness = 1
          AND deletedAtMillis IS NULL
    """)
    fun getTotalFixedExpensesBetween(
        startMillis: Long,
        endMillis: Long
    ): Flow<Double?>

    @Query("""
        SELECT SUM(expenseAmount)
        FROM expense_table
        WHERE dateMillis BETWEEN :startMillis AND :endMillis
          AND isBusiness = 0
          AND deletedAtMillis IS NULL
    """)
    fun getTotalPersonalExpensesBetween(
        startMillis: Long,
        endMillis: Long
    ): Flow<Double?>

    @Query("""
        SELECT IFNULL(SUM(expenseAmount), 0)
        FROM expense_table
        WHERE isBusiness = 1
          AND deletedAtMillis IS NULL
    """)
    fun getTotalFixedExpensesAll(): Flow<Double>

    @Query("""
        SELECT IFNULL(SUM(expenseAmount), 0)
        FROM expense_table
        WHERE isBusiness = 0
          AND deletedAtMillis IS NULL
    """)
    fun getTotalPersonalExpensesAll(): Flow<Double>

    @Query("""
        SELECT IFNULL(SUM(expenseAmount), 0)
        FROM expense_table
        WHERE deletedAtMillis IS NULL
    """)
    fun getTotalExpensesAll(): Flow<Double>

    @Query("""
        SELECT IFNULL(SUM(expenseAmount), 0)
        FROM expense_table
        WHERE dateMillis = :dateMillis
          AND isBusiness = 1
          AND deletedAtMillis IS NULL
    """)
    fun getTotalBusinessExpensesDaily(
        dateMillis: Long
    ): Flow<Double>

    @Query("""
        SELECT IFNULL(SUM(expenseAmount), 0)
        FROM expense_table
        WHERE dateMillis = :dateMillis
          AND deletedAtMillis IS NULL
    """)
    fun getTotalExpensesDaily(
        dateMillis: Long
    ): Flow<Double>
}


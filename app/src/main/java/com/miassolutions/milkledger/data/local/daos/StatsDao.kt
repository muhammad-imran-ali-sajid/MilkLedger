package com.miassolutions.milkledger.data.local.daos


import androidx.room.Dao
import androidx.room.Query
import com.miassolutions.milkledger.presentation.stats.StateRecord
import java.time.LocalDate

@Dao
interface StatsDao {

    /* -------------------------
       CUSTOMER TOTALS
    ------------------------- */

    @Query("""
        SELECT c.customerName AS name,
               SUM(s.paid) AS amount,
               SUM(s.volume) AS volume,
               'CUSTOMER' AS category
        FROM sales_table s
        INNER JOIN customer_table c
            ON c.customerId = s.customerId
        WHERE s.dateMillis BETWEEN :startMillis AND :endMillis
          AND s.deletedAtMillis IS NULL
        GROUP BY c.customerId
        ORDER BY c.sortOrder
    """)
    suspend fun getCustomerTotals(
        startMillis: Long,
        endMillis: Long
    ): List<StateRecord>

    /* -------------------------
       SUPPLIER TOTALS
    ------------------------- */

    @Query("""
        SELECT sup.supplierName AS name,
               SUM(p.payment) AS amount,
               SUM(p.milkAmount) AS volume,
               'SUPPLIER' AS category
        FROM purchase_table p
        INNER JOIN supplier_table sup
            ON sup.supplierId = p.supplierId
        WHERE p.dateMillis BETWEEN :startMillis AND :endMillis
          AND p.deletedAtMillis IS NULL
        GROUP BY sup.supplierId
        ORDER BY sup.sortOrder
    """)
    suspend fun getSupplierTotals(
        startMillis: Long,
        endMillis: Long
    ): List<StateRecord>

    /* -------------------------
       BUSINESS EXPENSES
    ------------------------- */

    @Query("""
        SELECT e.expenseTitle AS name,
               SUM(e.expenseAmount) AS amount,
               'EXPENSE' AS category
        FROM expense_table e
        WHERE e.dateMillis BETWEEN :startMillis AND :endMillis
          AND e.isDefault = 1
          AND e.deletedAtMillis IS NULL
        GROUP BY e.expenseTitle
        ORDER BY e.expenseTitle ASC
    """)
    suspend fun getBusinessExpenseTotals(
        startMillis: Long,
        endMillis: Long
    ): List<StateRecord>

    /* -------------------------
       PERSONAL EXPENSES
    ------------------------- */

    @Query("""
        SELECT e.expenseTitle AS name,
               SUM(e.expenseAmount) AS amount,
               'OTHER' AS category
        FROM expense_table e
        WHERE e.dateMillis BETWEEN :startMillis AND :endMillis
          AND e.isDefault = 0
          AND e.deletedAtMillis IS NULL
        GROUP BY e.expenseTitle
        ORDER BY e.expenseTitle ASC
    """)
    suspend fun getPersonalExpenseTotals(
        startMillis: Long,
        endMillis: Long
    ): List<StateRecord>

    /* -------------------------
       PROFIT TOTALS (derived)
    ------------------------- */

    @Query("""
        SELECT p.dateMillis AS name,
               SUM(p.netProfit) AS amount,
               'PROFIT' AS category
        FROM profit_table p
        WHERE p.dateMillis BETWEEN :startMillis AND :endMillis
          AND p.deletedAtMillis IS NULL
        GROUP BY p.dateMillis
    """)
    suspend fun getProfitTotals(
        startMillis: Long,
        endMillis: Long
    ): List<StateRecord>
}



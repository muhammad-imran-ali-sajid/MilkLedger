package com.miassolutions.milkledger.data.local.daos


import androidx.room.Dao
import androidx.room.Query
import com.miassolutions.milkledger.presentation.stats.StateRecord
import java.time.LocalDate

@Dao
interface StateDao {

    // -------------------------
    // CUSTOMER SUMMARIES
    // -------------------------
    @Query("""
        SELECT c.customerName AS name,
               SUM(s.paid) AS amount,
               SUM(s.volume) AS volume,
               'CUSTOMER' AS category
        FROM sales_table s
        INNER JOIN customer_table c 
            ON c.customerId = s.customerId
        WHERE s.date BETWEEN :start AND :end
        GROUP BY c.customerId
    """)
    suspend fun getCustomerTotals(start: LocalDate, end: LocalDate): List<StateRecord>


    // -------------------------
    // SUPPLIER SUMMARIES
    // -------------------------
    @Query("""
        SELECT sup.supplierName AS name,
               SUM(p.payment) AS amount,
               SUM(p.milkAmount) AS volume,
               'SUPPLIER' AS category
        FROM purchase_table p
        INNER JOIN supplier_table sup
            ON sup.supplierId = p.supplierId
        WHERE p.date BETWEEN :start AND :end
        GROUP BY sup.supplierId
    """)
    suspend fun getSupplierTotals(start: LocalDate, end: LocalDate): List<StateRecord>


    // -------------------------
    // BUSINESS EXPENSES
    // -------------------------
    @Query("""
        SELECT e.expenseTitle AS name,
               SUM(e.expenseAmount) AS amount,
               'EXPENSE' AS category
        FROM expense_table e
        WHERE e.date BETWEEN :start AND :end
          AND e.isDefault = 1
        GROUP BY  e.expenseTitle
        ORDER BY e.expenseTitle ASC
    """)
    suspend fun getBusinessExpenseTotals(start: LocalDate, end: LocalDate): List<StateRecord>

    // -------------------------
    // PERSONAL EXPENSES
    // -------------------------
    @Query("""
        SELECT e.expenseTitle AS name,
               SUM(e.expenseAmount) AS amount,
               'OTHER' AS category
        FROM expense_table e
        WHERE e.date BETWEEN :start AND :end
          AND e.isDefault = 0
        GROUP BY e.expenseTitle
        ORDER BY e.expenseTitle ASC
    """)
    suspend fun getPersonalExpenseTotals(start: LocalDate, end: LocalDate): List<StateRecord>


    @Query("""
        SELECT p.receivedDate AS name,
               SUM(p.receivedProfit) AS amount,
               'PROFIT' AS category
        FROM profit_table p
        WHERE p.receivedDate BETWEEN :start AND :end
        GROUP BY p.receivedProfit
    """)
    suspend fun getProfitTotals(start: LocalDate, end: LocalDate): List<StateRecord>
}


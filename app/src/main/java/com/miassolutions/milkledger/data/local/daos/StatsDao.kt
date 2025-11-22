package com.miassolutions.milkledger.data.local.daos


import androidx.room.Dao
import androidx.room.Query
import com.miassolutions.milkledger.presentation.stats.StateRecord
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface StateDao {

    // -------------------------
    // CUSTOMER SUMMARIES
    // -------------------------
    @Query("""
        SELECT c.customerName AS name,
               SUM(s.paid) AS amount,
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
               'SUPPLIER' AS category
        FROM purchase_table p
        INNER JOIN supplier_table sup
            ON sup.supplierId = p.supplierId
        WHERE p.date BETWEEN :start AND :end
        GROUP BY sup.supplierId
    """)
    suspend fun getSupplierTotals(start: LocalDate, end: LocalDate): List<StateRecord>


    // -------------------------
    // EXPENSE SUMMARIES
    // -------------------------
    @Query("""
        SELECT e.expenseTitle AS name,
               SUM(e.expenseAmount) AS amount,
               'EXPENSE' AS category
        FROM expense_table e
        WHERE e.date BETWEEN :start AND :end
        GROUP BY e.expenseAmount
    """)
    suspend fun getExpenseTotals(start: LocalDate, end: LocalDate): List<StateRecord>
}


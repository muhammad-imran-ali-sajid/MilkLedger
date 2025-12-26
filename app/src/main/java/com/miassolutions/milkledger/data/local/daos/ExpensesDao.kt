package com.miassolutions.milkledger.data.local.daos

import androidx.room.*
import com.miassolutions.milkledger.presentation.expenses.data.ExpensesEntity
import com.miassolutions.milkledger.presentation.stats.BusinessExpenseSummary
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.YearMonth

@Dao
interface ExpensesDao {

    /* ---------------------------------------------------
       Aggregates
    --------------------------------------------------- */

    @Query("""
        SELECT IFNULL(SUM(expenseAmount), 0) 
        FROM expense_table 
        WHERE isBusiness = 1 
          AND deletedAtMillis IS NULL
    """)
    fun observeBusinessExpenses(): Flow<Double>

    @Query("""
        SELECT IFNULL(SUM(expenseAmount), 0) 
        FROM expense_table 
        WHERE isBusiness = 0 
          AND deletedAtMillis IS NULL
    """)
    fun observePersonalExpenses(): Flow<Double>

    /* ---------------------------------------------------
       Sync / Raw access
    --------------------------------------------------- */

    @Query("SELECT * FROM expense_table")
    suspend fun getAllExpensesList(): List<ExpensesEntity>

    @Query("DELETE FROM expense_table")
    suspend fun clearAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(expenses: List<ExpensesEntity>)

    @Upsert
    suspend fun upsert(expense: ExpensesEntity)

    @Query("""
        UPDATE expense_table 
        SET deletedAtMillis = :deletedAtMillis 
        WHERE expenseId = :id
    """)
    suspend fun softDeleteById(id: String, deletedAtMillis: Long)

    /* ---------------------------------------------------
       Daily
    --------------------------------------------------- */

    @Query("""
        SELECT * FROM expense_table
        WHERE dateMillis = :dateMillis
          AND deletedAtMillis IS NULL
        ORDER BY expenseAmount DESC
    """)
    fun getDailyExpenses(dateMillis: Long): Flow<List<ExpensesEntity>>

    @Query("""
        SELECT expenseTitle 
        FROM expense_table
        WHERE dateMillis = :dateMillis
          AND deletedAtMillis IS NULL
    """)
    suspend fun getTitlesForDate(dateMillis: Long): List<String>

    /* ---------------------------------------------------
       Fixed vs Variable
    --------------------------------------------------- */

    @Query("""
        SELECT * FROM expense_table
        WHERE isBusiness = 1
          AND dateMillis = :dateMillis
          AND deletedAtMillis IS NULL
    """)
    fun getFixedExpenses(dateMillis: Long): Flow<List<ExpensesEntity>>

    @Query("""
        SELECT IFNULL(SUM(expenseAmount), 0)
        FROM expense_table
        WHERE isBusiness = 1
          AND dateMillis = :dateMillis
          AND deletedAtMillis IS NULL
    """)
    fun getFixedExpensesTotal(dateMillis: Long): Flow<Double>

    @Query("""
        SELECT * FROM expense_table
        WHERE isBusiness = 0
          AND dateMillis = :dateMillis
          AND deletedAtMillis IS NULL
    """)
    fun getVariableExpenses(dateMillis: Long): Flow<List<ExpensesEntity>>

    @Query("""
        SELECT IFNULL(SUM(expenseAmount), 0)
        FROM expense_table
        WHERE isBusiness = 0
          AND dateMillis = :dateMillis
          AND deletedAtMillis IS NULL
    """)
    fun getVariableExpensesTotal(dateMillis: Long): Flow<Double>

    /* ---------------------------------------------------
       Between dates (range queries)
    --------------------------------------------------- */

    @Query("""
        SELECT IFNULL(SUM(expenseAmount), 0)
        FROM expense_table
        WHERE dateMillis BETWEEN :startMillis AND :endMillis
          AND deletedAtMillis IS NULL
    """)
    suspend fun getExpensesTotalBetween(
        startMillis: Long,
        endMillis: Long
    ): Double

    /* ---------------------------------------------------
       Single item
    --------------------------------------------------- */

    @Query("""
        SELECT * FROM expense_table
        WHERE expenseId = :id
          AND deletedAtMillis IS NULL
        LIMIT 1
    """)
    suspend fun getExpenseById(id: String): ExpensesEntity?

    @Query("""
        SELECT EXISTS(
            SELECT 1 FROM expense_table
            WHERE expenseTitle = :title
              AND dateMillis = :dateMillis
              AND deletedAtMillis IS NULL
        )
    """)
    suspend fun expenseExistsForTitleAndDate(
        title: String,
        dateMillis: Long
    ): Boolean
}


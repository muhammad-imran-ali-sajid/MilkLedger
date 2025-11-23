package com.miassolutions.milkledger.data.local.daos

import androidx.room.*
import com.miassolutions.milkledger.data.local.entities.ExpensesEntity
import com.miassolutions.milkledger.presentation.stats.BusinessExpenseSummary
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface ExpensesDao {

    // Retrieve all entities as a List for synchronization purposes
    @Query("SELECT * FROM expense_table")
    suspend fun getAllExpensesList(): List<ExpensesEntity>

    @Query("DELETE FROM expense_table")
    suspend fun clearAll()

    // Inserts a list of entities, replacing existing ones (upsert)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(expenses: List<ExpensesEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpensesEntity)

    @Update
    suspend fun updateExpense(expense: ExpensesEntity)

    @Delete
    suspend fun deleteExpense(expense: ExpensesEntity)

    @Query("DELETE FROM expense_table WHERE expenseId = :id")
    suspend fun deleteById(id: String)

    // All expenses for a specific date
    @Query(
        """
        SELECT * FROM expense_table 
        WHERE date = :date AND deletedAt IS NULL
    """
    )
    fun getAllExpenses(date: LocalDate): Flow<List<ExpensesEntity>>


    @Query("SELECT expenseTitle FROM expense_table WHERE date = :date")
    suspend fun getTitlesForDate(date: LocalDate): List<String>


    // Summary for charts/list
    @Query(
        """
        SELECT expenseTitle, expenseAmount 
        FROM expense_table 
        WHERE date = :date AND deletedAt IS NULL
        ORDER BY expenseAmount DESC
    """
    )
    fun getTotalExpenses(date: LocalDate): Flow<List<BusinessExpenseSummary>>

    @Query(
        """
        SELECT * FROM expense_table 
        WHERE expenseId = :id 
        LIMIT 1
    """
    )
    suspend fun getExpenseById(id: String): ExpensesEntity?

    @Query(
        """
        SELECT EXISTS(
            SELECT 1 FROM expense_table 
            WHERE expenseTitle = :title 
              AND date = :date 
              AND deletedAt IS NULL
            LIMIT 1
        )
    """
    )
    suspend fun expenseExistsForTitleAndDate(title: String, date: LocalDate): Boolean


    // Total between dates
    @Query(
        """
        SELECT SUM(expenseAmount) 
        FROM expense_table 
        WHERE date BETWEEN :start AND :end 
          AND deletedAt IS NULL
    """
    )
    suspend fun getExpensesTotalBetween(start: LocalDate, end: LocalDate): Double?


    // -------------------------------------------------------------
    // 🔥 NEW: FIXED vs VARIABLE expenses using isDefault
    // -------------------------------------------------------------

    // FIXED EXPENSES (Fuel, Meal…)
    @Query(
        """
        SELECT * FROM expense_table
        WHERE isDefault = 1 
          AND date = :date 
          AND deletedAt IS NULL
    """
    )
    fun getFixedExpenses(date: LocalDate): Flow<List<ExpensesEntity>>

    @Query(
        """
        SELECT SUM(expenseAmount) 
        FROM expense_table
        WHERE isDefault = 1
          AND date = :date
          AND deletedAt IS NULL
    """
    )
    fun getFixedExpensesTotal(date: LocalDate): Flow<Double?>


    // VARIABLE EXPENSES
    @Query(
        """
        SELECT * FROM expense_table
        WHERE isDefault = 0 
          AND date = :date 
          AND deletedAt IS NULL
    """
    )
    fun getVariableExpenses(date: LocalDate): Flow<List<ExpensesEntity>>

    @Query(
        """
        SELECT SUM(expenseAmount) 
        FROM expense_table
        WHERE isDefault = 0
          AND date = :date
          AND deletedAt IS NULL
    """
    )
    fun getVariableExpensesTotal(date: LocalDate): Flow<Double?>
}

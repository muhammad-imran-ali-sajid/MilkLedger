package com.miassolutions.milkledger.data.local.daos

import androidx.room.*
import com.miassolutions.milkledger.data.local.entities.ExpensesEntity
import com.miassolutions.milkledger.presentation.stats.ExpenseSummary
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

    @Query("SELECT * FROM expense_table WHERE date = :date")
    fun getAllExpenses(date : LocalDate): Flow<List<ExpensesEntity>>

    @Query("""
    SELECT 
        expenseTitle, 
        expenseAmount 
    FROM 
        expense_table 
    WHERE 
        date = :date AND deletedAt IS NULL  -- Use the :date parameter here
    ORDER BY 
        expenseAmount DESC
""")
    fun getTotalExpenses(date : LocalDate): Flow<List<ExpenseSummary>>

    @Query("SELECT * FROM expense_table WHERE expenseId = :id LIMIT 1")
    suspend fun getExpenseById(id: String): ExpensesEntity?

    @Query("SELECT EXISTS(SELECT 1 FROM expense_table WHERE expenseTitle = :title AND date = :date LIMIT 1)")
    suspend fun expenseExistsForTitleAndDate(title: String, date: LocalDate): Boolean

    @Query("SELECT SUM(expenseAmount) FROM expense_table WHERE date BETWEEN :start AND :end")
    suspend fun getExpensesTotalBetween(start: LocalDate, end: LocalDate): Double?

}
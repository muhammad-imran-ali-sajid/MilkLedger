package com.miassolutions.milkledger.data.local.daos

import androidx.room.*
import com.miassolutions.milkledger.data.local.entities.ExpensesEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface ExpensesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpensesEntity)

    @Update
    suspend fun updateExpense(expense: ExpensesEntity)

    @Delete
    suspend fun deleteExpense(expense: ExpensesEntity)

    @Query("SELECT * FROM expense_table WHERE date = :date")
    fun getAllExpenses(date : LocalDate): Flow<List<ExpensesEntity>>

    @Query("SELECT * FROM expense_table WHERE expenseId = :id LIMIT 1")
    suspend fun getExpenseById(id: String): ExpensesEntity?

    @Query("SELECT EXISTS(SELECT 1 FROM expense_table WHERE expenseTitle = :title AND date = :date LIMIT 1)")
    suspend fun expenseExistsForTitleAndDate(title: String, date: LocalDate): Boolean

}

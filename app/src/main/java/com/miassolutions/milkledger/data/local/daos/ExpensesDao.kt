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

    @Query("SELECT * FROM ExpensesEntity WHERE date = :date")
    fun getAllExpenses(date : LocalDate): Flow<List<ExpensesEntity>>

    @Query("SELECT * FROM ExpensesEntity WHERE expenseId = :id LIMIT 1")
    suspend fun getExpenseById(id: String): ExpensesEntity?
}

package com.miassolutions.milkledger.core.localdb.expense

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    
    @Query("SELECT * FROM expense_table")
    suspend fun getAllExpensesForBackup(): List<ExpenseEntity>

    // ------------------------------------------------
    // 1️⃣ WRITE
    // ------------------------------------------------
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(expenses: List<ExpenseEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity)

    @Update
    suspend fun updateExpense(expense: ExpenseEntity)

    @Query("UPDATE expense_table SET deletedAtMillis = :deletedAt, isSynced = 0 WHERE expenseId = :id")
    suspend fun softDeleteExpense(id: String, deletedAt: Long)

    // ------------------------------------------------
    // 2️⃣ READ LISTS
    // ------------------------------------------------

    // Saare Kharche (Business + Personal mixed)
    @Query("""
        SELECT * FROM expense_table 
        WHERE dateMillis BETWEEN :start AND :end 
        AND deletedAtMillis IS NULL 
        ORDER BY dateMillis DESC
    """)
    fun getExpensesByDateRange(start: Long, end: Long): Flow<List<ExpenseEntity>>

    // Sirf Dukan ke Kharche (Filter by Type)
    @Query("""
        SELECT * FROM expense_table 
        WHERE isPersonal = 0 
        AND deletedAtMillis IS NULL 
        ORDER BY dateMillis DESC
    """)
    fun getAllBusinessExpenses(): Flow<List<ExpenseEntity>>

    // Sirf Ghar ke Kharche
    @Query("""
        SELECT * FROM expense_table 
        WHERE isPersonal = 1 
        AND deletedAtMillis IS NULL 
        ORDER BY dateMillis DESC
    """)
    fun getAllPersonalExpenses(): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expense_table WHERE expenseId = :id")
    suspend fun getExpenseById(id: String): ExpenseEntity?

    // ------------------------------------------------
    // 3️⃣ REPORTS
    // ------------------------------------------------

    // Total Kharcha aaj ka (Repository mein Business vs Personal separate kar lenge)
    @Query("""
        SELECT SUM(amount) FROM expense_table 
        WHERE dateMillis BETWEEN :start AND :end 
        AND deletedAtMillis IS NULL
    """)
    fun getTotalExpenseAmount(start: Long, end: Long): Flow<Double?>

    // ------------------------------------------------
    // 4️⃣ SYNC
    // ------------------------------------------------
    @Query("SELECT * FROM expense_table WHERE isSynced = 0")
    suspend fun getUnsyncedExpenses(): List<ExpenseEntity>

    @Query("UPDATE expense_table SET isSynced = 1 WHERE expenseId = :id")
    suspend fun markAsSynced(id: String)
}
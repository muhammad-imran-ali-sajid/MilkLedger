package com.miassolutions.milkledger.data.local.daos

import androidx.room.*
import com.miassolutions.milkledger.data.local.entities.ExpensesEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpensesDao {

    // ------------------------------------------------
    // 1️⃣ WRITE
    // ------------------------------------------------
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpensesEntity)

    @Update
    suspend fun updateExpense(expense: ExpensesEntity)

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
    fun getExpensesByDateRange(start: Long, end: Long): Flow<List<ExpensesEntity>>

    // Sirf Dukan ke Kharche (Filter by Type)
    @Query("""
        SELECT * FROM expense_table 
        WHERE isBusiness = 1 
        AND deletedAtMillis IS NULL 
        ORDER BY dateMillis DESC
    """)
    fun getAllBusinessExpenses(): Flow<List<ExpensesEntity>>

    // Sirf Ghar ke Kharche
    @Query("""
        SELECT * FROM expense_table 
        WHERE isBusiness = 0 
        AND deletedAtMillis IS NULL 
        ORDER BY dateMillis DESC
    """)
    fun getAllPersonalExpenses(): Flow<List<ExpensesEntity>>

    @Query("SELECT * FROM expense_table WHERE expenseId = :id")
    suspend fun getExpenseById(id: String): ExpensesEntity?

    // ------------------------------------------------
    // 3️⃣ REPORTS
    // ------------------------------------------------

    // Total Kharcha aaj ka (Repository mein Business vs Personal separate kar lenge)
    @Query("""
        SELECT SUM(expenseAmount) FROM expense_table 
        WHERE dateMillis BETWEEN :start AND :end 
        AND deletedAtMillis IS NULL
    """)
    fun getTotalExpenseAmount(start: Long, end: Long): Flow<Double?>

    // ------------------------------------------------
    // 4️⃣ SYNC
    // ------------------------------------------------
    @Query("SELECT * FROM expense_table WHERE isSynced = 0")
    suspend fun getUnsyncedExpenses(): List<ExpensesEntity>

    @Query("UPDATE expense_table SET isSynced = 1 WHERE expenseId = :id")
    suspend fun markAsSynced(id: String)
}


package com.miassolutions.milkledger.data.repository


import com.miassolutions.milkledger.data.local.daos.ExpensesDao
import com.miassolutions.milkledger.data.local.entities.ExpensesEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExpensesRepository @Inject constructor(
    private val expensesDao: ExpensesDao
) {

    fun getAllExpensesForDate(date: LocalDate): Flow<List<ExpensesEntity>> =
        expensesDao.getAllExpenses(date)

    suspend fun insertExpense(expense: ExpensesEntity) {
        expensesDao.insertExpense(expense)
    }

    suspend fun updateExpense(expense: ExpensesEntity) {
        expensesDao.updateExpense(expense)
    }

    suspend fun deleteExpense(expense: ExpensesEntity) {
        expensesDao.deleteExpense(expense)
    }

    suspend fun getExpenseById(id: String): ExpensesEntity? {
        return expensesDao.getExpenseById(id)
    }

    suspend fun expenseExistsForTitleAndDate(title: String, date: LocalDate) =
        expensesDao.expenseExistsForTitleAndDate(title, date)
}

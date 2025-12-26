package com.miassolutions.milkledger.presentation.expenses.data

import android.util.Log
import com.miassolutions.milkledger.core.extensions.toMillis
import com.miassolutions.milkledger.data.local.daos.ExpensesDao
import com.miassolutions.milkledger.data.local.daos.TransactionDao
import com.miassolutions.milkledger.data.local.entities.TransactionEntity
import com.miassolutions.milkledger.data.local.entities.TransactionType
import com.miassolutions.milkledger.data.mapper.toDomain
import com.miassolutions.milkledger.data.remote.FirestoreSyncHelper
import com.miassolutions.milkledger.domain.model.Expense
import com.miassolutions.milkledger.domain.model.Transaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExpenseRepository @Inject constructor(
    private val expenseDao: ExpensesDao,
    private val transactionDao: TransactionDao,
    private val firestore: FirestoreSyncHelper
) {

    companion object {
        private const val TAG = "ExpenseRepository"
        private const val COLLECTION = "expenses"
    }

    /* ---------------------------------------------------
       READ
    --------------------------------------------------- */

    fun getExpensesByDate(date: LocalDate): Flow<List<Expense>> =
        expenseDao.getDailyExpenses(date.toMillis())
            .map { list -> list.map { it.toDomain() } }

    fun getFixedExpenses(date: LocalDate): Flow<List<Expense>> =
        expenseDao.getFixedExpenses(date.toMillis())
            .map { list -> list.map { it.toDomain() } }

    fun getVariableExpenses(date: LocalDate): Flow<List<Expense>> =
        expenseDao.getVariableExpenses(date.toMillis())
            .map { list -> list.map { it.toDomain() } }

    suspend fun expenseExists(title: String, date: LocalDate): Boolean =
        expenseDao.expenseExistsForTitleAndDate(
            title = title,
            dateMillis = date.toMillis()
        )

    /* ---------------------------------------------------
       WRITE : EXPENSE
    --------------------------------------------------- */

    suspend fun insertExpense(expense: Expense) {
        val entity = expense.toEntity()

        // 1️⃣ Save expense
        expenseDao.upsert(entity)

        // 2️⃣ Ledger entry (EXPENSE = money OUT)
        transactionDao.insert(
            TransactionEntity(
                dateMillis = entity.dateMillis,
                type = TransactionType.EXPENSE,
                referenceId = entity.expenseId,
                debit = entity.expenseAmount,
                credit = 0.0,
                profitImpact = -entity.expenseAmount,
                note = entity.expenseTitle
            )
        )

        // 3️⃣ Firestore (best-effort)
//        syncSafely {
//            firestore.uploadSingle(
//                collectionName = COLLECTION,
//                documentId = entity.expenseId,
//                data = entity.toFirestoreModel() // mapper later
//            )
//        }
    }

    suspend fun updateExpense(expense: Expense) {
        val entity = expense.toEntity()

        expenseDao.upsert(entity)

        // Append-only ledger
        transactionDao.insert(
            TransactionEntity(
                dateMillis = entity.dateMillis,
                type = TransactionType.EXPENSE,
                referenceId = entity.expenseId,
                debit = entity.expenseAmount,
                credit = 0.0,
                profitImpact = -entity.expenseAmount,
                note = "Expense updated"
            )
        )
    }

    suspend fun deleteExpense(expenseId: String) {
        val deletedAt = System.currentTimeMillis()

        expenseDao.softDeleteById(expenseId, deletedAt)

        // 🆕 Ledger reversal
        transactionDao.insert(
            TransactionEntity(
                dateMillis = deletedAt,
                type = TransactionType.EXPENSE_REVERSAL,
                referenceId = expenseId,
                debit = 0.0,
                credit = 0.0,
                profitImpact = 0.0,
                note = "Expense deleted"
            )
        )

        syncSafely {
            firestore.deleteDocument(COLLECTION, expenseId)
        }
    }

    /* ---------------------------------------------------
       ADJUSTMENTS
    --------------------------------------------------- */

    suspend fun addExpenseAdjustment(
        expenseId: String,
        amount: Double,
        note: String?
    ) {
        if (amount == 0.0) return
        if (expenseDao.getExpenseById(expenseId) == null) return

        transactionDao.insert(
            TransactionEntity(
                dateMillis = System.currentTimeMillis(),
                type = TransactionType.PROFIT_ADJUSTMENT,
                referenceId = expenseId,
                debit = if (amount > 0) amount else 0.0,
                credit = if (amount < 0) -amount else 0.0,
                profitImpact = -amount,
                note = note
            )
        )
    }

    fun observeExpenseAdjustments(expenseId: String): Flow<List<Transaction>> =
        transactionDao.getAdjustmentsFor(expenseId)
            .map { list -> list.map { it.toDomain() } }

    /* ---------------------------------------------------
       HELPERS
    --------------------------------------------------- */

    private suspend fun syncSafely(block: suspend () -> Unit) {
        try {
            block()
        } catch (e: Exception) {
            Log.e(TAG, "Firestore sync failed", e)
        }
    }
}

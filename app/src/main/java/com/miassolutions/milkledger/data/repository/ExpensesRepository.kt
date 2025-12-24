package com.miassolutions.milkledger.data.repository

import android.util.Log
import com.miassolutions.milkledger.data.local.daos.ExpensesDao
import com.miassolutions.milkledger.data.local.entities.ExpensesEntity
import com.miassolutions.milkledger.data.oldmapper.toFirestoreModel
import com.miassolutions.milkledger.data.oldmapper.toFirestoreModelList
import com.miassolutions.milkledger.data.remote.FirestoreSyncHelper
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExpensesRepository @Inject constructor(
    private val dao: ExpensesDao,
    private val firestore: FirestoreSyncHelper
) {

    companion object {
        private const val COLLECTION = "expenses"
        private const val TAG = "ExpensesRepository"
    }

    // ----------------------------------------------------------
    // READ OPERATIONS
    // ----------------------------------------------------------

    fun getDailyExpenses(date: LocalDate): Flow<List<ExpensesEntity>> =
        dao.getDailyExpenses(date)

    suspend fun getMonthlyExpenses(date: LocalDate): List<ExpensesEntity> {
        val ym = "${date.year}-${"%02d".format(date.monthValue)}"
        return dao.getMonthlyExpenses(ym)
    }

    fun getFixedExpenses(date: LocalDate): Flow<List<ExpensesEntity>> =
        dao.getFixedExpenses(date)

    fun getVariableExpenses(date: LocalDate): Flow<List<ExpensesEntity>> =
        dao.getVariableExpenses(date)

    suspend fun getExpenseById(id: String): ExpensesEntity? =
        dao.getExpenseById(id)

    fun getAllExpensesForDate(date: LocalDate): Flow<List<ExpensesEntity>> =
        dao.getAllExpenses(date)

    suspend fun getAllExpensesList(): List<ExpensesEntity> =
        dao.getAllExpensesList()

    // ----------------------------------------------------------
    // WRITE OPERATIONS + FIRESTORE SYNC
    // ----------------------------------------------------------

    suspend fun upsertExpense(expense: ExpensesEntity) {
        val final = expense.copy(
            updatedAt = LocalDateTime.now().toString()
        )

        dao.upsert(final) // Insert or update

        try {
            firestore.uploadSingle(
                collectionName = COLLECTION,
                documentId = final.expenseId,
                data = final.toFirestoreModel()
            )
        } catch (e: Exception) {
            Log.e(TAG, "Sync failed for upsert: ${expense.expenseId}", e)
        }
    }

    suspend fun upsertAllExpenses(expenses: List<ExpensesEntity>) {
        dao.upsertAll(expenses)

        try {
            firestore.uploadCollection(
                collectionName = COLLECTION,
                dataList = expenses.toFirestoreModelList(),
                idExtractor = { it.id } // Firestore document id
            )
        } catch (e: Exception) {
            Log.e(TAG, "Batch sync failed", e)
        }
    }

    suspend fun deleteExpense(expense: ExpensesEntity) {
        dao.deleteExpense(expense)

        try {
            firestore.deleteDocument(
                collectionName = COLLECTION,
                documentId = expense.expenseId
            )
        } catch (e: Exception) {
            Log.e(TAG, "Sync failed for delete: ${expense.expenseId}", e)
        }
    }

    suspend fun insertAll(expenses: List<ExpensesEntity>) {
        dao.upsertAll(expenses)

        try {
            firestore.uploadCollection(
                collectionName = COLLECTION,
                dataList = expenses,
                idExtractor = { it.expenseId }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Batch sync failed", e)
        }
    }

    // ----------------------------------------------------------
    // FULL SYNC / RESTORE
    // ----------------------------------------------------------

    /**
     * Full restore from Firestore (useful on reinstall).
     */
    suspend fun restoreAllExpenses() {
        Log.d(TAG, "Restoring expenses from Firestore...")
        try {
            val remoteExpenses = firestore.downloadCollection<ExpensesEntity>(COLLECTION)
            if (remoteExpenses.isNotEmpty()) {
                dao.upsertAll(remoteExpenses)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to restore expenses", e)
        }
    }

    /**
     * Full two-way sync (optional periodic backup).
     */
    suspend fun synchronizeExpenses() {
        Log.d(TAG, "Starting full sync for expenses...")

        try {
            // 1️⃣ Download remote
            val remote = firestore.downloadCollection<ExpensesEntity>(COLLECTION)
            if (remote.isNotEmpty()) {
                dao.upsertAll(remote)
            }

            // 2️⃣ Upload local
            val local = dao.getAllExpensesList()
            firestore.uploadCollection(
                collectionName = COLLECTION,
                dataList = local.toFirestoreModelList(),
                idExtractor = { it.id }
            )

        } catch (e: Exception) {
            Log.e(TAG, "Full sync failed", e)
        }
    }
}

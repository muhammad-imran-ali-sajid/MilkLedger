package com.miassolutions.milkledger.data.repository

import android.util.Log
import com.miassolutions.milkledger.data.local.daos.ExpensesDao
import com.miassolutions.milkledger.data.local.entities.ExpensesEntity
import com.miassolutions.milkledger.data.remote.FirestoreSyncHelper
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExpensesRepository @Inject constructor(
    private val expensesDao: ExpensesDao,
    private val firestoreSyncHelper: FirestoreSyncHelper
) {
    companion object {
        private const val EXPENSES_COLLECTION = "expenses"
        private const val TAG = "ExpensesRepository"
    }

    // --- Local Read Operations (Offline-First) ---

    fun getAllExpensesForDate(date: LocalDate): Flow<List<ExpensesEntity>> =
        expensesDao.getAllExpenses(date)

    suspend fun getExpenseById(id: String): ExpensesEntity? {
        return expensesDao.getExpenseById(id)
    }

    suspend fun expenseExistsForTitleAndDate(title: String, date: LocalDate) =
        expensesDao.expenseExistsForTitleAndDate(title, date)

    // --- Local Write Operations + Remote Synchronization ---

    /**
     * Inserts an expense locally, then attempts to upload it to Firestore.
     */
    suspend fun insertExpense(expense: ExpensesEntity) {
        expensesDao.insertExpense(expense)
        try {
            // Assuming ExpensesEntity has an 'id' field used as the documentId
            firestoreSyncHelper.uploadSingle(
                collectionName = EXPENSES_COLLECTION,
                documentId = expense.expenseId,
                data = expense
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync insert for expense ID: ${expense.expenseId}", e)
            // Log the error but allow the local operation to succeed (offline-first)
        }
    }

    /**
     * Updates an expense locally, then attempts to update it on Firestore.
     */
    suspend fun updateExpense(expense: ExpensesEntity) {
        expensesDao.updateExpense(expense)
        try {
            firestoreSyncHelper.uploadSingle(
                collectionName = EXPENSES_COLLECTION,
                documentId = expense.expenseId,
                data = expense
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync update for expense ID: ${expense.expenseId}", e)
            // Log the error but allow the local operation to succeed (offline-first)
        }
    }

    /**
     * Deletes an expense locally, then attempts to delete it from Firestore.
     */
    suspend fun deleteExpense(expense: ExpensesEntity) {
        val expenseId = expense.expenseId
        expensesDao.deleteExpense(expense)

        try {
            // Delete the entity from the remote database
            firestoreSyncHelper.deleteDocument(
                collectionName = EXPENSES_COLLECTION,
                documentId = expenseId
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync delete for expense ID: $expenseId", e)
            // The local delete succeeded; we log the remote failure. A dedicated background
            // sync worker should handle retries for failed remote operations.
        }
    }

    // --- Full Synchronization ---

    /**
     * Synchronizes all local expenses with the remote Firestore database.
     * This function should ideally be called periodically or when the app starts/regains connectivity.
     */
    suspend fun synchronizeExpenses() {
        Log.d(TAG, "Starting full expense synchronization...")
        try {
            // 1. Download and merge remote changes (simple implementation: merge unique items)
            val remoteExpenses = firestoreSyncHelper.downloadCollection<ExpensesEntity>(EXPENSES_COLLECTION)
            if (remoteExpenses.isNotEmpty()) {
                // Conflict resolution strategy: remote updates overwrite local or new remote items are inserted.
                // For simplicity, we just insert/update all remote items locally.
                expensesDao.upsertAll(remoteExpenses)
                Log.d(TAG, "Downloaded and merged ${remoteExpenses.size} items from Firestore.")
            } else {
                Log.d(TAG, "No remote items found to download.")
            }

            // 2. Upload all local changes (ensuring all local data is pushed)
            val allLocalExpenses = expensesDao.getAllExpensesList() // Needs a DAO function to get all as a list
            if (allLocalExpenses.isNotEmpty()) {
                firestoreSyncHelper.uploadCollection(
                    collectionName = EXPENSES_COLLECTION,
                    dataList = allLocalExpenses,
                    idExtractor = { it.expenseId } // Explicitly use the 'id' field
                )
                Log.d(TAG, "Uploaded ${allLocalExpenses.size} local items to Firestore.")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Full synchronization failed: ${e.localizedMessage}", e)
            // The system remains operational due to local data, but sync failed.
        }
    }
}
package com.miassolutions.milkledger.data.repository

import android.util.Log
import com.miassolutions.milkledger.data.local.daos.CustomerDao
import com.miassolutions.milkledger.data.local.daos.ExpensesDao
import com.miassolutions.milkledger.data.local.daos.NoteDao
import com.miassolutions.milkledger.data.local.daos.SupplierDao
import com.miassolutions.milkledger.data.mapper.toEntity
import com.miassolutions.milkledger.data.mapper.toEntityModel
import com.miassolutions.milkledger.data.mapper.toRoomEntity
import com.miassolutions.milkledger.data.remote.FirestoreSyncHelper
import com.miassolutions.milkledger.data.remote.model.FirestoreCustomer
import com.miassolutions.milkledger.data.remote.model.FirestoreExpense
import com.miassolutions.milkledger.data.remote.model.FirestoreNotes
import com.miassolutions.milkledger.data.remote.model.FirestoreSupplier
import jakarta.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

// DataRepository.kt (Refined)

class DataRepository @Inject constructor(
    private val syncHelper: FirestoreSyncHelper,
    private val supplierDao: SupplierDao, // Injected Room DAO
    private val customerDao: CustomerDao,
    private val expenseDao: ExpensesDao,
    private val notesDao: NoteDao
    // ... inject other DAOs here later (e.g., productDao)
) {
    // ... AppData, toRoomEntity() mapper (as extension function) ...

    /**
     * Downloads ALL collections required for the app and saves them to Room.
     */
    suspend fun syncAllDataFromCloud() = coroutineScope {
        // --- 1. DOWNLOAD SUPPLIERS ---
        val suppliersDeferred = async {
            syncHelper.downloadCollection<FirestoreSupplier>("suppliers")
        }

        val customersDeferred = async {
            syncHelper.downloadCollection<FirestoreCustomer>("customers")
        }

        val notesDeferred = async {
            syncHelper.downloadCollection<FirestoreNotes>("notes")
        }

        val expensesDeferred = async {
            syncHelper.downloadCollection<FirestoreExpense>("expenses")
        }

        // --- 2. DOWNLOAD OTHER COLLECTIONS HERE (e.g., Products) ---
        // val productsDeferred = async {
        //     syncHelper.downloadCollection<FirestoreProduct>("products")
        // }

        // Await all downloads concurrently
        val downloadedSuppliers = suppliersDeferred.await()
        val downloadedCustomers = customersDeferred.await()
        val downloadedExpenses = expensesDeferred.await()
        val downloadedNotes = notesDeferred.await()

        // --- 3. PROCESS AND SAVE SUPPLIERS ---
        val supplierEntities = downloadedSuppliers.map { it.toRoomEntity() }
        supplierDao.upsertAll(supplierEntities)
        Log.d("DataRepository", "Saved ${supplierEntities.size} suppliers to Room.")

        val customersEntities = downloadedCustomers.map { it.toRoomEntity() }
        customerDao.upsertAll(customersEntities)
        Log.d("DataRepository", "Saved ${customersEntities.size} customers to Room.")

        val notesEntities = downloadedNotes.map { it.toEntity() }
        notesDao.upsertAll(notesEntities)
        Log.d("DataRepository", "Saved ${notesEntities.size} notes to Room.")

        val expensesEntities = downloadedExpenses.map { it.toEntityModel() }
        expenseDao.upsertAll(expensesEntities)
        Log.d("DataRepository", "Saved ${expensesEntities.size} expenses to Room.")

        // --- 4. PROCESS AND SAVE PRODUCTS ---
        // val productEntities = downloadedProducts.map { it.toRoomEntity() }
        // productDao.upsertAll(productEntities)
        // Log.d("DataRepository", "Saved ${productEntities.size} products to Room.")

        // Return a success status or nothing
        return@coroutineScope // Signifies completion
    }
}


// A container to hold all fetched data
data class AppData(
    val suppliers: List<FirestoreSupplier>,
    val customers: List<FirestoreCustomer>,
    val notes: List<FirestoreNotes>
)
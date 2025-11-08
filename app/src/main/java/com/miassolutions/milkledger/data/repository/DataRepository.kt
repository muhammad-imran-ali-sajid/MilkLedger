package com.miassolutions.milkledger.data.repository

import com.miassolutions.milkledger.data.local.entities.ExpensesEntity
import com.miassolutions.milkledger.data.local.entities.NoteEntity
import com.miassolutions.milkledger.data.remote.FirestoreSyncHelper
import com.miassolutions.milkledger.domain.model.Customer
import com.miassolutions.milkledger.domain.model.Supplier
import jakarta.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class DataRepository @Inject constructor(
    private val syncHelper: FirestoreSyncHelper
) {
    suspend fun fetchAllAppCollections(): AppData = coroutineScope {

        // Use async to start each network call concurrently
        val customersDeferred = async { syncHelper.downloadCollection<Customer>("customers") }
        val suppliersDeferred = async { syncHelper.downloadCollection<Supplier>("suppliers") }
        val notesDeferred = async { syncHelper.downloadCollection<NoteEntity>("notes") }
        val expensesDeferred = async { syncHelper.downloadCollection<ExpensesEntity>("expenses") }
        // ... add more collections here

        // Await on all deferred results to ensure all network calls complete
        AppData(
            customers = customersDeferred.await(),
            suppliers = suppliersDeferred.await(),
            notes = notesDeferred.await(),
            expenses = expensesDeferred.await()
        )
    }
}



// A container to hold all fetched data
data class AppData(
    val customers: List<Customer>,
    val suppliers: List<Supplier>,
    val notes: List<NoteEntity>,
    val expenses: List<ExpensesEntity>
    // ... add more collections here
)
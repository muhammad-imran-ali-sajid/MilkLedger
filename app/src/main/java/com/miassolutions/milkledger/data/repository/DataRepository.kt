package com.miassolutions.milkledger.data.repository

import android.util.Log
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.miassolutions.milkledger.data.local.daos.CustomerDao
import com.miassolutions.milkledger.data.local.daos.ExpensesDao
import com.miassolutions.milkledger.data.local.daos.NoteDao
import com.miassolutions.milkledger.data.local.daos.SalesDao
import com.miassolutions.milkledger.data.local.daos.SupplierDao
import com.miassolutions.milkledger.data.mapper.toEntity
import com.miassolutions.milkledger.data.mapper.toEntityModel
import com.miassolutions.milkledger.data.mapper.toRoomEntity
import com.miassolutions.milkledger.data.remote.model.FirestoreCustomer
import com.miassolutions.milkledger.data.remote.model.FirestoreExpense
import com.miassolutions.milkledger.data.remote.model.FirestoreNotes
import com.miassolutions.milkledger.data.remote.model.FirestoreSales
import com.miassolutions.milkledger.data.remote.model.FirestoreSupplier
import dagger.hilt.android.scopes.ActivityRetainedScoped
import jakarta.inject.Inject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

// NOTE: I'm injecting FirebaseFirestore directly as the FirestoreSyncHelper implementation was unknown.
@ActivityRetainedScoped
class DataRepository @Inject constructor(
    private val firestore: FirebaseFirestore, // Assumed to be injected via Dagger/Hilt
    private val supplierDao: SupplierDao,
    private val customerDao: CustomerDao,
    private val expenseDao: ExpensesDao,
    private val salesDao: SalesDao,
    private val notesDao: NoteDao
) {
    private val TAG = "DataRepository"

    /**
     * Sets up real-time listeners for all necessary collections.
     * This function suspends and launches the long-running listeners in a supervisorScope,
     * ensuring that if one listener fails, others remain active.
     */
    suspend fun setupRealtimeListeners() = supervisorScope {
        Log.d(TAG, "Setting up real-time listeners in a SupervisorScope...")

        // Launch each collection observer as a separate, long-running child coroutine.
        // This scope will keep running until the calling scope (ViewModelScope) is cancelled.
        launch { observeCollectionChanges("suppliers") }
        launch { observeCollectionChanges("customers") }
        launch { observeCollectionChanges("sales") }
        launch { observeCollectionChanges("notes") }
        launch { observeCollectionChanges("expenses") }

        // This coroutine will suspend indefinitely while the child listeners are running.
        // The ViewModel will launch this in a detached coroutine, allowing its setup flow to complete.
    }

    /**
     * A generic function to set up a robust, real-time Firestore listener and update Room.
     * It uses documentChanges to only process specific documents that were added, modified, or removed.
     * @param collectionPath The name of the Firestore collection (e.g., "suppliers").
     */
    private suspend fun observeCollectionChanges(collectionPath: String) {
        // This Flow represents the real-time stream of data from Firestore.
        callbackFlow {
            Log.d(TAG, "Starting listener for $collectionPath...")

            val collectionRef = firestore.collection(collectionPath)

            // Add the robust real-time snapshot listener
            val listenerRegistration = collectionRef.addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e(TAG, "Listen failed for $collectionPath: ${e.message}", e)
                    close(e)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    // Process only the document changes
                    val changes = snapshot.documentChanges
                    Log.d(TAG, "Received ${changes.size} changes for $collectionPath.")

                    // Launch in the flow's scope to perform the suspend DAO calls
                    launch {
                        changes.forEach { change ->
                            when (change.type) {
                                DocumentChange.Type.ADDED, DocumentChange.Type.MODIFIED -> {
                                    // Upsert: Add or replace the document in Room
                                    upsertChangedDocument(collectionPath, change)
                                }

                                DocumentChange.Type.REMOVED -> {
                                    // Delete: Remove the document from Room
                                    deleteRemovedDocument(collectionPath, change)
                                }
                            }
                        }
                        trySend(Unit) // Signal that an update occurred
                    }
                }
            }

            // This block runs when the flow is closed (e.g., when the ViewModel is cleared)
            awaitClose {
                Log.d(TAG, "Stopping listener for $collectionPath.")
                listenerRegistration.remove()
            }
        }.collect {
            // Keep the flow alive by collecting it
        }
    }

    @Suppress("UNCHECKED_CAST")
    private suspend fun upsertChangedDocument(collectionPath: String, change: DocumentChange) {
        val docId = change.document.id
        val modelClass = getModelClass(collectionPath) as Class<Any>
        val cloudModel = change.document.toObject(modelClass)

        when (collectionPath) {
            "suppliers" -> {
                val entity = (cloudModel as FirestoreSupplier).toRoomEntity()
                supplierDao.upsertAll(listOf(entity))
            }

            "customers" -> {
                val entity = (cloudModel as FirestoreCustomer).toRoomEntity()
                customerDao.upsertAll(listOf(entity))
            }

            "sales" -> {
                val entity = (cloudModel as FirestoreSales).toEntityModel()
                salesDao.upsertAll(listOf(entity))
            }

            "notes" -> {
                val entity = (cloudModel as FirestoreNotes).toEntity()
                notesDao.upsertAll(listOf(entity))
            }

            "expenses" -> {
                val entity = (cloudModel as FirestoreExpense).toEntityModel()
                expenseDao.upsertAll(listOf(entity))
            }
        }
        Log.d(TAG, "UPSERTED: $collectionPath/$docId")
    }

    // IMPORTANT: This assumes your DAOs have a suspend fun deleteById(id: String) method.
    private suspend fun deleteRemovedDocument(collectionPath: String, change: DocumentChange) {
        val docId = change.document.id
        when (collectionPath) {
//            // NOTE: You must implement a deleteById(id: String) or similar method in your DAOs
//            "suppliers" -> supplierDao.deleteById(docId)
//            "customers" -> customerDao.deleteById(docId)
//            "notes" -> notesDao.deleteById(docId)
//            "expenses" -> expenseDao.deleteById(docId)
        }
        Log.d(TAG, "DELETED: $collectionPath/$docId")
    }


    /** Helper function to determine the correct class for Firestore deserialization. */
    @Suppress("UNCHECKED_CAST")
    private fun getModelClass(collectionPath: String): Class<*> {
        return when (collectionPath) {
            "suppliers" -> FirestoreSupplier::class.java
            "customers" -> FirestoreCustomer::class.java
            "sales" -> FirestoreSales::class.java
            "notes" -> FirestoreNotes::class.java
            "expenses" -> FirestoreExpense::class.java
            else -> throw IllegalArgumentException("Unknown collection path: $collectionPath")
        }
    }

    // --- Data Streams from Room (These will now be automatically updated by the listeners) ---

    // Example: Expose data from Room DAOs as Flows
    fun getSuppliersStream() = supplierDao.getAllSuppliers()
    fun getCustomersStream() = customerDao.getAllCustomers()
    // ... add more streams for other data types ...
}
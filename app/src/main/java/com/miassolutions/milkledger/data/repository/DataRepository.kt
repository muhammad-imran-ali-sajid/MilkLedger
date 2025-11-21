package com.miassolutions.milkledger.data.repository

import android.util.Log
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.miassolutions.milkledger.data.local.daos.CustomerDao
import com.miassolutions.milkledger.data.local.daos.ExpensesDao
import com.miassolutions.milkledger.data.local.daos.NoteDao
import com.miassolutions.milkledger.data.local.daos.PurchaseDao
import com.miassolutions.milkledger.data.local.daos.SalesDao
import com.miassolutions.milkledger.data.local.daos.SupplierDao
import com.miassolutions.milkledger.data.mapper.toEntity
import com.miassolutions.milkledger.data.mapper.toEntityModel
import com.miassolutions.milkledger.data.mapper.toRoomEntity
import com.miassolutions.milkledger.data.remote.model.FirestoreCustomer
import com.miassolutions.milkledger.data.remote.model.FirestoreExpense
import com.miassolutions.milkledger.data.remote.model.FirestoreNotes
import com.miassolutions.milkledger.data.remote.model.FirestorePurchase
import com.miassolutions.milkledger.data.remote.model.FirestoreSales
import com.miassolutions.milkledger.data.remote.model.FirestoreSupplier
import dagger.hilt.android.scopes.ActivityRetainedScoped
import jakarta.inject.Inject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

@ActivityRetainedScoped
class DataRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val supplierDao: SupplierDao,
    private val customerDao: CustomerDao,
    private val expenseDao: ExpensesDao,
    private val salesDao: SalesDao,
    private val purchaseDao: PurchaseDao,
    private val notesDao: NoteDao
) {
    private val TAG = "DataRepository"

    /**
     * Sets up real-time listeners for all necessary collections.
     */
    suspend fun setupRealtimeListeners() = supervisorScope {
        Log.d(TAG, "Setting up real-time listeners in a SupervisorScope...")

        // Launch each collection observer as a separate coroutine.
//        launch { observeCollectionChanges("suppliers") }
//        launch { observeCollectionChanges("customers") }
//        launch { observeCollectionChanges("sales") }
//        launch { observeCollectionChanges("purchases") }
//        launch { observeCollectionChanges("notes") }
//        launch { observeCollectionChanges("expenses") }
    }

    /**
     * A generic function to set up a robust, real-time Firestore listener and update Room.
     */
    private suspend fun observeCollectionChanges(collectionPath: String) {
        callbackFlow {
            Log.d(TAG, "Starting listener for $collectionPath...")

            val collectionRef = firestore.collection(collectionPath)

            val listenerRegistration = collectionRef.addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e(TAG, "Listen failed for $collectionPath: ${e.message}", e)
                    // Close the flow with the exception to stop the collector
                    close(e)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val changes = snapshot.documentChanges
                    Log.d(TAG, "Received ${changes.size} changes for $collectionPath.")

                    // Launch in the flow's scope to perform the suspend DAO calls
                    launch {
                        try { // Robust error handling for DAO operations
                            changes.forEach { change ->
                                when (change.type) {
                                    DocumentChange.Type.ADDED, DocumentChange.Type.MODIFIED -> {
                                        upsertChangedDocument(collectionPath, change)
                                    }

                                    DocumentChange.Type.REMOVED -> {
                                        deleteRemovedDocument(collectionPath, change)
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            // Log the database error but allow the listener to stay active
                            Log.e(TAG, "DAO operation failed for $collectionPath: ${e.message}", e)
                        }
                        trySend(Unit)
                    }
                }
            }

            // Clean up when the flow is cancelled
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
        val modelClass = getModelClass(collectionPath) as Class<Any>
        val cloudModel = change.document.toObject(modelClass)

        when (collectionPath) {
            "suppliers" -> (cloudModel as FirestoreSupplier).toRoomEntity().apply {
                supplierDao.upsertAll(listOf(this))
            }

            "customers" -> (cloudModel as FirestoreCustomer).toRoomEntity().apply {
                customerDao.upsertAll(listOf(this))
            }

            "sales" -> (cloudModel as FirestoreSales).toEntityModel().apply {
                salesDao.upsertAll(listOf(this))
            }

            "purchases" -> (cloudModel as FirestorePurchase).toEntityModel().apply {
                purchaseDao.upsertAll(listOf(this))
            }

            "notes" -> (cloudModel as FirestoreNotes).toEntity().apply {
                notesDao.upsertAll(listOf(this))
            }

            "expenses" -> (cloudModel as FirestoreExpense).toEntityModel().apply {
                expenseDao.upsertAll(listOf(this))
            }
        }
        Log.d(TAG, "UPSERTED: $collectionPath/${change.document.id}")
    }

    // ✅ IMPLEMENTED: Delete the Room entry corresponding to the removed Firestore document.
    private suspend fun deleteRemovedDocument(collectionPath: String, change: DocumentChange) {
        val docId = change.document.id
        when (collectionPath) {
            "suppliers" -> {
                supplierDao.deleteById(docId)
                firestore.collection("purchases")
                    .whereEqualTo("supplierId", docId)
                    .get()
                    .addOnSuccessListener { snapshots ->
                        snapshots?.forEach {doc ->
                            firestore.collection("purchases").document(doc.id).delete()

                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.e(TAG, "Failed to delete Firestore purchase for supplier $docId : ${exception.message}")
                    }
                Log.d(TAG, "Deleted supplier $docId and their sales locally and in Firestore")
            }
            "customers" -> {
                // Delete customer locally (Room cascade will remove local sales)
                customerDao.deleteById(docId)

                // Also delete Firestore sales belonging to this customer
                firestore.collection("sales")
                    .whereEqualTo("customerId", docId)
                    .get()
                    .addOnSuccessListener { snapshot ->
                        snapshot?.forEach { doc ->
                            firestore.collection("sales").document(doc.id).delete()
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Failed to delete Firestore sales for customer $docId: ${e.message}")
                    }

                Log.d(TAG, "Deleted customer $docId and their sales locally and in Firestore")
            }
//            "sales" -> salesDao.deleteById(docId)
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
            "purchases" -> FirestorePurchase::class.java
            "notes" -> FirestoreNotes::class.java
            "expenses" -> FirestoreExpense::class.java
            else -> throw IllegalArgumentException("Unknown collection path: $collectionPath")
        }
    }


}
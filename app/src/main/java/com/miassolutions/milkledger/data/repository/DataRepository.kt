package com.miassolutions.milkledger.data.repository

import android.content.SharedPreferences
import android.util.Log
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.miassolutions.milkledger.core.di.IoDispatcher
import com.miassolutions.milkledger.data.local.daos.*
import com.miassolutions.milkledger.data.mapper.toEntity
import com.miassolutions.milkledger.data.mapper.toEntityModel
import com.miassolutions.milkledger.data.mapper.toRoomEntity
import com.miassolutions.milkledger.data.remote.FirestoreCollections.PROFITS
import com.miassolutions.milkledger.data.remote.FirestoreCollections.EXPENSES
import com.miassolutions.milkledger.data.remote.FirestoreCollections.NOTES
import com.miassolutions.milkledger.data.remote.FirestoreCollections.SALES
import com.miassolutions.milkledger.data.remote.FirestoreCollections.PURCHASES
import com.miassolutions.milkledger.data.remote.FirestoreCollections.SUPPLIERS
import com.miassolutions.milkledger.data.remote.FirestoreCollections.CUSTOMERS
import com.miassolutions.milkledger.data.remote.model.*
import kotlinx.coroutines.*
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val supplierDao: SupplierDao,
    private val customerDao: CustomerDao,
    private val expenseDao: ExpensesDao,
    private val salesDao: SalesDao,
    private val purchaseDao: PurchaseDao,
    private val notesDao: NoteDao,
    private val profitDao: ProfitDao,
    private val prefs: SharedPreferences,
    @IoDispatcher private val io: CoroutineDispatcher
) {

    private val TAG = "RepoOptimized"

    private val PREF_INITIAL_SYNC = "initial_sync_done"

    private val listenerMap = ConcurrentHashMap<String, ListenerRegistration>()
    private val skipFirstSnapshot = ConcurrentHashMap<String, Boolean>()

    private val job = SupervisorJob()
    private val scope = CoroutineScope(io + job)

    @Volatile private var initialized = false

    /** Call this ONCE per app start (Application.onCreate). */
    fun initialize() {
        if (initialized) return
        initialized = true

        scope.launch {
            val firstSyncNeeded = !prefs.getBoolean(PREF_INITIAL_SYNC, false)

            if (firstSyncNeeded) {
                Log.d(TAG, "Initial sync required → performing full sync")
                performInitialFullSync()
                prefs.edit().putBoolean(PREF_INITIAL_SYNC, true).apply()
            }

            Log.d(TAG, "Attaching real-time listeners")
            attachAllListeners()
        }
    }

    // ----------------------------------------------------------------------
    // INITIAL FULL SYNC
    // ----------------------------------------------------------------------

    private suspend fun performInitialFullSync() = withContext(io) {
        val jobs = listOf(
            launch { syncOnce(SUPPLIERS) },
            launch { syncOnce(CUSTOMERS) },
            launch { syncOnce(SALES) },
            launch { syncOnce(PURCHASES) },
            launch { syncOnce(NOTES) },
            launch { syncOnce(EXPENSES) },
            launch { syncOnce(PROFITS) }
        )
        jobs.joinAll()
    }

    private suspend fun syncOnce(collectionPath: String) = withContext(io) {
        try {
            Log.d(TAG, "syncOnce: $collectionPath")
            val snapshot = Tasks.await(firestore.collection(collectionPath).get())

            val docs = snapshot.documents
            Log.d(TAG, "syncOnce: fetched ${docs.size} docs for $collectionPath")

            when (collectionPath) {

                SUPPLIERS -> supplierDao.upsertAll(
                    docs.mapNotNull { it.toObject(FirestoreSupplier::class.java)?.toRoomEntity() }
                )

                CUSTOMERS -> customerDao.upsertAll(
                    docs.mapNotNull { it.toObject(FirestoreCustomer::class.java)?.toRoomEntity() }
                )

                SALES -> salesDao.upsertAll(
                    docs.mapNotNull { it.toObject(FirestoreSales::class.java)?.toEntityModel() }
                )

                PURCHASES -> purchaseDao.upsertAll(
                    docs.mapNotNull { it.toObject(FirestorePurchase::class.java)?.toEntityModel() }
                )

                NOTES -> notesDao.upsertAll(
                    docs.mapNotNull { it.toObject(FirestoreNotes::class.java)?.toEntity() }
                )

                EXPENSES -> expenseDao.upsertAll(
                    docs.mapNotNull { it.toObject(FirestoreExpense::class.java)?.toEntityModel() }
                )

                PROFITS -> profitDao.upsertAll(
                    docs.mapNotNull { it.toObject(FirestoreProfit::class.java)?.toEntity() }
                )
            }

            skipFirstSnapshot[collectionPath] = true

        } catch (e: Exception) {
            Log.e(TAG, "syncOnce failed for $collectionPath: ${e.message}", e)
        }
    }

    // ----------------------------------------------------------------------
    // REAL-TIME LISTENERS
    // ----------------------------------------------------------------------

    private fun attachAllListeners() {
        attachListener(SUPPLIERS)
        attachListener(CUSTOMERS)
        attachListener(SALES)
        attachListener(PURCHASES)
        attachListener(NOTES)
        attachListener(EXPENSES)
        attachListener(PROFITS)
    }

    private fun attachListener(collectionPath: String) {
        if (listenerMap.containsKey(collectionPath)) return

        Log.d(TAG, "Starting listener for $collectionPath")

        val reg = firestore.collection(collectionPath)
            .addSnapshotListener { snapshot, e ->

                if (e != null) {
                    Log.e(TAG, "Listen failed for $collectionPath: ${e.message}")
                    return@addSnapshotListener
                }
                if (snapshot == null) return@addSnapshotListener

                // Skip initial snapshot (we already synced data)
                if (skipFirstSnapshot.remove(collectionPath) == true) {
                    Log.d(TAG, "Skipping first snapshot for $collectionPath")
                    return@addSnapshotListener
                }

                val changes = snapshot.documentChanges
                if (changes.isEmpty()) return@addSnapshotListener

                scope.launch {
                    changes.forEach { change ->
                        when (change.type) {
                            DocumentChange.Type.ADDED,
                            DocumentChange.Type.MODIFIED -> upsertChanged(collectionPath, change)

                            DocumentChange.Type.REMOVED -> deleteChanged(collectionPath, change)
                        }
                    }
                }
            }

        listenerMap[collectionPath] = reg
    }

    fun stopListeners() {
        listenerMap.values.forEach { it.remove() }
        listenerMap.clear()
        Log.d(TAG, "All listeners stopped")
    }

    // ----------------------------------------------------------------------
    // UPSERT & DELETE HANDLERS
    // ----------------------------------------------------------------------

    private suspend fun upsertChanged(collection: String, change: DocumentChange) = withContext(io) {
        val doc = change.document

        when (collection) {

            SUPPLIERS -> supplierDao.upsertAll(
                listOf(doc.toObject(FirestoreSupplier::class.java).toRoomEntity())
            )

            CUSTOMERS -> customerDao.upsertAll(
                listOf(doc.toObject(FirestoreCustomer::class.java).toRoomEntity())
            )

            SALES -> salesDao.upsertAll(
                listOf(doc.toObject(FirestoreSales::class.java).toEntityModel())
            )

            PURCHASES -> purchaseDao.upsertAll(
                listOf(doc.toObject(FirestorePurchase::class.java).toEntityModel())
            )

            NOTES -> notesDao.upsertAll(
                listOf(doc.toObject(FirestoreNotes::class.java).toEntity())
            )

            EXPENSES -> expenseDao.upsertAll(
                listOf(doc.toObject(FirestoreExpense::class.java).toEntityModel())
            )

            PROFITS -> profitDao.upsertAll(
                listOf(doc.toObject(FirestoreProfit::class.java).toEntity())
            )
        }

        Log.d(TAG, "UPSERTED: $collection/${doc.id}")
    }

    private suspend fun deleteChanged(collection: String, change: DocumentChange) = withContext(io) {
        val id = change.document.id

        when (collection) {
            SUPPLIERS -> supplierDao.deleteById(id)
            CUSTOMERS -> customerDao.deleteById(id)
            SALES -> salesDao.deleteSale(id)
            PURCHASES -> purchaseDao.deletePurchase(id)
            NOTES -> notesDao.deleteById(id)
            EXPENSES -> expenseDao.deleteById(id)
            PROFITS -> profitDao.deleteProfit(id)
        }

        Log.d(TAG, "DELETED: $collection/$id")
    }
}

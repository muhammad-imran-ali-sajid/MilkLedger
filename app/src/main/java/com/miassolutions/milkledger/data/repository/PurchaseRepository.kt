package com.miassolutions.milkledger.data.repository

import android.util.Log
import com.google.firebase.firestore.Source
import com.miassolutions.milkledger.data.local.daos.PurchaseDao
import com.miassolutions.milkledger.data.local.entities.PurchaseEntity
import com.miassolutions.milkledger.data.local.entities.SupplierEntity
import com.miassolutions.milkledger.data.local.relations.PurchaseWithSupplier
import com.miassolutions.milkledger.data.remote.FirestoreSyncHelper
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PurchaseRepository @Inject constructor(
    private val purchaseDao: PurchaseDao,
    private val firestoreSyncHelper: FirestoreSyncHelper
) {

    private companion object {
        private const val TAG = "PurchaseRepository"
        private const val PURCHASE_COLLECTION = "purchases"
    }

    // --- Local Read Operations (Offline-First Read) ---

    suspend fun getPurchasesByDateOnce(date: LocalDate): List<PurchaseWithSupplier> =
        purchaseDao.getPurchasesByDateOnce(date)

    fun getAllSuppliers(): Flow<List<SupplierEntity>> =
        purchaseDao.getAllSuppliers()

    // 🧾 All purchases for reports or dashboard
    fun getAllPurchasesWithSuppliers(): Flow<List<PurchaseWithSupplier>> =
        purchaseDao.getAllPurchasesWithSuppliers()

    // 📅 For current date screen (daily ledger)
    fun getPurchasesByDate(date: LocalDate): Flow<List<PurchaseWithSupplier>> =
        purchaseDao.getPurchasesByDate(date)

    // 👤 For supplier ledger details
    fun getPurchasesForSupplier(supplierId: String): Flow<List<PurchaseWithSupplier>> =
        purchaseDao.getPurchasesForSupplier(supplierId)

    // --- Local Write Operations + Remote Synchronization Trigger ---

    // 🟢 Insert new purchase (used when a supplier first added today)
    suspend fun insertPurchase(purchase: PurchaseEntity) {
        // 1. Local write for immediate UI update
        try {
            purchaseDao.insertPurchase(purchase)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to insert purchase locally: ${purchase.purchaseId}", e)
            return
        }

        // 2. Initiate remote write (Firestore handles the background sync)
        try {
            firestoreSyncHelper.uploadSingle(PURCHASE_COLLECTION, purchase.purchaseId, purchase)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync inserted purchase to Firestore: ${purchase.purchaseId}", e)
        }
    }

    // 🟡 Update live changes (fat, lr, volume, notes)
    suspend fun updatePurchase(purchase: PurchaseEntity) {
        // 1. Local write for immediate UI update
        try {
            purchaseDao.updatePurchase(purchase)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update purchase locally: ${purchase.purchaseId}", e)
            return
        }

        // 2. Initiate remote write
        try {
            firestoreSyncHelper.uploadSingle(PURCHASE_COLLECTION, purchase.purchaseId, purchase)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync updated purchase to Firestore: ${purchase.purchaseId}", e)
        }
    }

    // 🔴 Delete entry
    suspend fun deletePurchase(purchaseId: String) {
        // 1. Local delete for immediate UI update
        try {
            purchaseDao.deletePurchase(purchaseId)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete purchase locally: $purchaseId", e)
            return
        }

        // 2. Initiate remote delete
        try {
            firestoreSyncHelper.deleteDocument(PURCHASE_COLLECTION, purchaseId)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync delete for purchase ID: $purchaseId", e)
        }
    }

    // --- Full Synchronization ---

    /**
     * Synchronizes all local purchases with the remote Firestore database.
     * This performs a full two-way sync: downloads remote changes and uploads all local changes.
     *
     * NOTE: This assumes PurchaseDao contains:
     * 1. `suspend fun upsertAll(purchases: List<PurchaseEntity>)` for merging remote data.
     * 2. `suspend fun getAllPurchasesList(): List<PurchaseEntity>` for uploading all local data.
     */
    suspend fun synchronizePurchases() {
        Log.d(TAG, "Starting full purchase synchronization...")
        try {
            // 1. Download and merge remote changes
            val remotePurchases = firestoreSyncHelper.downloadCollection<PurchaseEntity>(
                PURCHASE_COLLECTION,

            )

            if (remotePurchases.isNotEmpty()) {
                purchaseDao.upsertAll(remotePurchases)
                Log.d(TAG, "Downloaded and merged ${remotePurchases.size} purchases from Firestore.")
            } else {
                Log.d(TAG, "No remote items found to download.")
            }

            // 2. Upload all local changes (ensuring all local data is pushed)
            val allLocalPurchases = purchaseDao.getAllPurchasesList()
            if (allLocalPurchases.isNotEmpty()) {
                firestoreSyncHelper.uploadCollection(
                    collectionName = PURCHASE_COLLECTION,
                    dataList = allLocalPurchases,
                    idExtractor = { it.purchaseId } // Explicitly use the 'purchaseId' field
                )
                Log.d(TAG, "Uploaded ${allLocalPurchases.size} local items to Firestore.")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Full synchronization failed: ${e.localizedMessage}", e)
        }
    }
}
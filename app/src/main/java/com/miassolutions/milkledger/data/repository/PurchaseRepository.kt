package com.miassolutions.milkledger.data.repository

import android.util.Log
import com.miassolutions.milkledger.data.local.daos.PurchaseDao
import com.miassolutions.milkledger.data.local.entities.PurchaseEntity
import com.miassolutions.milkledger.data.local.entities.SupplierEntity
import com.miassolutions.milkledger.data.local.relations.PurchaseWithSupplier
import com.miassolutions.milkledger.data.mapper.toFirestoreModel
import com.miassolutions.milkledger.data.remote.FirestoreSyncHelper
import com.miassolutions.milkledger.domain.model.Supplier
import com.miassolutions.milkledger.presentation.stats.SupplierPaidSummary
import com.miassolutions.milkledger.presentation.supplier.BalanceHistory
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PurchaseRepository @Inject constructor(
    private val purchaseDao: PurchaseDao,

    private val firestoreSyncHelper: FirestoreSyncHelper // Used for local-write-first remote PUSH
) {

    private companion object {
        private const val TAG = "PurchaseRepository"
        private const val PURCHASE_COLLECTION = "purchases"
    }

    fun getPaidToSuppliersForDate(targetDate: LocalDate): Flow<List<SupplierPaidSummary>> {
        return purchaseDao.getPaidAmountToSupplierForDate(targetDate)
    }

    suspend fun isDuplicatePurchase(supplierId: String, date: LocalDate): Boolean =
        purchaseDao.countPurchaseForDate(supplierId, date) > 0

    fun observeSuppliersList(): Flow<List<SupplierEntity>> = purchaseDao.observeSuppliersList()

    fun getBalanceHistory(supplierId: String): Flow<List<BalanceHistory>> {
        return purchaseDao.getSupplierBalanceHistory(supplierId)
    }

    suspend fun getBalanceHistoryOnce(supplierId: String): List<PurchaseWithSupplier> = purchaseDao.getSupplierHistoryOnce(supplierId)

    // --- Write/Update/Delete Operations (Local Write First, Then Remote Sync) ---
    // Actions that originate from the local user MUST push data to Firestore.

    /**
     * Inserts a new purchase: Local write first (via upsert), then initiate remote sync.
     */
    suspend fun insertPurchase(purchase: PurchaseEntity) {
        // 1. Local write for immediate UI update (using upsertAll for consistency with DataRepository)
        try {
            purchaseDao.upsertAll(listOf(purchase)) // ⬅️ Using upsertAll
            Log.d(TAG, "Inserted purchase locally: ${purchase.purchaseId}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to insert purchase locally: ${purchase.purchaseId}", e)
            return
        }

        // 2. Initiate remote write
        try {
            // Assumes PurchaseEntity has a toFirestoreModel() mapper
            val firestorePurchase = purchase.toFirestoreModel()
            firestoreSyncHelper.uploadSingle(
                PURCHASE_COLLECTION, documentId = purchase.purchaseId,
                data = firestorePurchase
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync inserted purchase to Firestore: ${purchase.purchaseId}", e)
        }
    }

    /**
     * Updates an existing purchase: Local write first (via upsert), then initiate remote sync.
     */
    suspend fun updatePurchase(purchase: PurchaseEntity) {
        // 1. Local write for immediate UI update
        try {
            purchaseDao.upsertAll(listOf(purchase)) // ⬅️ Using upsertAll
            Log.d(TAG, "Updated purchase locally: ${purchase.purchaseId}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update purchase locally: ${purchase.purchaseId}", e)
            return
        }

        // 2. Initiate remote write
        try {
            val firestorePurchase = purchase.toFirestoreModel()
            firestoreSyncHelper.uploadSingle(
                PURCHASE_COLLECTION, documentId = purchase.purchaseId,
                data = firestorePurchase
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync updated purchase to Firestore: ${purchase.purchaseId}", e)
        }
    }

    /**
     * Deletes a purchase: Local delete first, then initiate remote sync.
     */
    suspend fun deletePurchase(purchaseId: String) {
        // 1. Local delete for immediate UI update
        try {
            // NOTE: Must ensure purchaseDao has 'deleteById(id: String)'
            purchaseDao.deletePurchase(purchaseId)
            Log.d(TAG, "Deleted purchase locally: $purchaseId")
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

    // --- Bulk Sync Operations (Retained for DataRepository) ---

    /**
     * Batch inserts or replaces (upserts) a collection of purchases into the local database.
     * This is called by the DataRepository's real-time listener to merge remote data.
     */
    suspend fun upsertAllPurchases(purchases: List<PurchaseEntity>) {
        purchaseDao.upsertAll(purchases)
    }

    /**
     * 🔥 REMOVED: synchronizePurchases()
     * This function is no longer necessary. The download synchronization is handled continuously
     * by DataRepository.setupRealtimeListeners().
     */


    // --- Read Operations (Remain purely local via Room/Flow) ---

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

    fun getAvgFat(date: LocalDate): Flow<Double?> = purchaseDao.getTotalFat(date)
    fun getAvgLr(date: LocalDate): Flow<Double?> = purchaseDao.getTotalLr(date)
    fun getAvgTs(date: LocalDate): Flow<Double?> = purchaseDao.getTotalTs(date)
    fun getTotalMilkWithFatLR(date: LocalDate): Flow<Double?> =
        purchaseDao.getTotalMilkWithFatLR(date)
}
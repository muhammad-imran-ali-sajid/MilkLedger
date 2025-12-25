package com.miassolutions.milkledger.data.repository

import android.util.Log
import com.miassolutions.milkledger.data.local.daos.ProfitDao
import com.miassolutions.milkledger.data.local.entities.ProfitEntity
import com.miassolutions.milkledger.data.mapper.toDomain
import com.miassolutions.milkledger.data.mapper.toEntity
import com.miassolutions.milkledger.data.remote.FirestoreSyncHelper
import com.miassolutions.milkledger.domain.model.Profit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfitRepository @Inject constructor(
    private val profitDao: ProfitDao, // received profits
    private val analytics: AnalyticsRepository,
    private val firestore: FirestoreSyncHelper
) {

    companion object {
        private const val COLLECTION = "profits"
        private const val TAG = "ProfitRepository"
    }

    // -------------------------
    // RECEIVED PROFIT (MANUAL)
    // -------------------------

    fun getAllReceivedProfits(): Flow<List<Profit>> =
        profitDao.getAllProfitFlow()
            .map { it.map { e -> e.toDomain() } }

    suspend fun upsertReceivedProfit(profit: Profit) {
        val entity = profit.toEntity()
        profitDao.upsert(entity)

//        syncSafely {
//            firestore.uploadSingle(
//                COLLECTION,
//                entity.profitId,
//                entity.toFirestore()
//            )
//        }
    }

    suspend fun deleteReceivedProfit(profitId: String) {
        val deletedAt = System.currentTimeMillis()
        profitDao.softDeleteById(profitId, deletedAt)

        syncSafely {
            firestore.deleteDocument(COLLECTION, profitId)
        }
    }

    // -------------------------
    // CALCULATED PROFIT (LEDGER)
    // -------------------------

    fun getNetProfitDaily(date: LocalDate): Flow<Double> =
        analytics.getNetProfitDaily(date)

    fun getNetProfitBetween(start: LocalDate, end: LocalDate): Flow<Double> =
        analytics.getNetProfitBetween(start, end)

    fun getNetProfitAll(): Flow<Double> =
        analytics.getNetProfitAll()

    // -------------------------
    // FINAL BALANCE (IMPORTANT)
    // -------------------------

    fun getRemainingProfitAll(): Flow<Double> =
        combine(
            analytics.getNetProfitAll(),
            profitDao.getTotalReceivedProfit()
        ) { calculated, received ->
            calculated - (received ?: 0.0)
        }

    // -------------------------
    // UTILS
    // -------------------------

    private suspend fun syncSafely(block: suspend () -> Unit) {
        try {
            block()
        } catch (e: Exception) {
            Log.e(TAG, "Firestore sync failed", e)
        }
    }
}

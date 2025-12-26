package com.miassolutions.milkledger.data.repository

import android.util.Log
import com.miassolutions.milkledger.core.extensions.toMillis
import com.miassolutions.milkledger.data.local.daos.SalesDao
import com.miassolutions.milkledger.data.local.daos.TransactionDao
import com.miassolutions.milkledger.data.local.entities.SalesEntity
import com.miassolutions.milkledger.data.local.entities.TransactionEntity
import com.miassolutions.milkledger.data.local.entities.TransactionType
import com.miassolutions.milkledger.data.mapper.toDomain
import com.miassolutions.milkledger.data.mapper.toEntity
import com.miassolutions.milkledger.data.remote.FirestoreSyncHelper
import com.miassolutions.milkledger.data.remote.mapper.FirestoreSaleModel
import com.miassolutions.milkledger.domain.model.Sale
import com.miassolutions.milkledger.domain.model.Transaction
import com.miassolutions.milkledger.presentation.expenses.data.toDomain
import com.miassolutions.milkledger.presentation.expenses.data.toEntity
import com.miassolutions.milkledger.presentation.stats.CustomerPaidSummary
import com.miassolutions.milkledger.presentation.supplier.balancehistory.BalanceHistory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SalesRepository @Inject constructor(
    private val salesDao: SalesDao,
    private val transactionDao: TransactionDao,
    private val firestore: FirestoreSyncHelper
) {

    companion object {
        private const val COLLECTION = "sales"
        private const val TAG = "SalesRepository"
    }

    /* ---------------------------------------------------
       READ
    --------------------------------------------------- */

    fun getPaidSalesForDate(date: LocalDate): Flow<List<CustomerPaidSummary>> =
        salesDao.getPaidAmountForDate(date.toMillis())

    fun getSalesByDate(date: LocalDate): Flow<List<Sale>> =
        salesDao.getSalesByDate(date.toMillis())
            .map { list -> list.map { it.sale.toDomain() } }

    fun getSalesForCustomer(customerId: String): Flow<List<Sale>> =
        salesDao.getSalesForCustomer(customerId)
            .map { list -> list.map { it.sale.toDomain() } }

    suspend fun getBalanceHistory(customerId: String): List<BalanceHistory> =
        salesDao.getCustomerBalanceHistory(customerId)

    suspend fun isDuplicateSale(customerId: String, date: LocalDate): Boolean =
        salesDao.countSalesForDate(customerId, date.toMillis()) > 0

    /* ---------------------------------------------------
       WRITE : SALE
    --------------------------------------------------- */

    suspend fun insertSale(sale: Sale) {
        val entity = sale.toEntity()

        // 1️⃣ Save sale
        salesDao.insertSale(entity)

        // 2️⃣ Ledger entry (SALE)
        transactionDao.insert(
            TransactionEntity(
                dateMillis = entity.dateMillis,
                type = TransactionType.SALE,
                referenceId = entity.saleId,
                debit = 0.0,
                credit = entity.paid,
                profitImpact = calculateProfit(entity),
                note = "Sale to customer ${entity.customerId}"
            )
        )

        // 3️⃣ Firestore (best-effort)
        syncSafely {
            firestore.uploadSingle(
                collectionName = COLLECTION,
                documentId = entity.saleId,
                data = FirestoreSaleModel.fromEntity(entity) // mapper later
            )
        }
    }

    suspend fun updateSale(sale: Sale) {
        val entity = sale.toEntity()

        salesDao.updateSale(entity)

        // Append-only ledger (do NOT edit old transaction)
        transactionDao.insert(
            TransactionEntity(
                dateMillis = entity.dateMillis,
                type = TransactionType.SALE,
                referenceId = entity.saleId,
                debit = 0.0,
                credit = entity.paid,
                profitImpact = calculateProfit(entity),
                note = "Sale updated"
            )
        )
    }

    suspend fun deleteSale(saleId: String) {
        val deletedAt = System.currentTimeMillis()

        salesDao.softDeleteSale(saleId, deletedAt)

        // Ledger reversal marker (audit safe)
        transactionDao.insert(
            TransactionEntity(
                dateMillis = deletedAt,
                type = TransactionType.SALE_REVERSAL,
                referenceId = saleId,
                debit = 0.0,
                credit = 0.0,
                profitImpact = 0.0,
                note = "Sale deleted"
            )
        )

        syncSafely {
            firestore.deleteDocument(COLLECTION, saleId)
        }
    }

    /* ---------------------------------------------------
       ADJUSTMENTS (MANUAL CORRECTIONS)
    --------------------------------------------------- */

    suspend fun addSaleAdjustment(
        saleId: String,
        amount: Double,
        note: String?
    ) {
        if (amount == 0.0) return

        // Guard: sale must exist
        if (salesDao.countSaleById(saleId) == 0) return

        transactionDao.insert(
            TransactionEntity(
                dateMillis = System.currentTimeMillis(),
                type = TransactionType.PROFIT_ADJUSTMENT,
                referenceId = saleId,
                debit = if (amount < 0) -amount else 0.0,
                credit = if (amount > 0) amount else 0.0,
                profitImpact = amount,
                note = note
            )
        )
    }

    fun observeSaleAdjustments(saleId: String): Flow<List<Transaction>> =
        transactionDao.getAdjustmentsFor(saleId)
            .map { list -> list.map { it.toDomain() } }

    suspend fun getFinalSaleAmount(sale: Sale): Double {
        val adjustment = transactionDao.getTotalAdjustmentFor(sale.id)
        return sale.price + adjustment
    }

    /* ---------------------------------------------------
       INTERNAL HELPERS
    --------------------------------------------------- */

    /**
     * SAFE default profit rule:
     * profit = cash received
     *
     * (Later you can evolve this using purchase cost avg)
     */
    private fun calculateProfit(sale: SalesEntity): Double {
        return sale.paid
    }

    private suspend fun syncSafely(block: suspend () -> Unit) {
        try {
            block()
        } catch (e: Exception) {
            Log.e(TAG, "Firestore sync failed", e)
        }
    }
}

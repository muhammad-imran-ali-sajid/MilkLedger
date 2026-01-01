package com.miassolutions.milkledger.data.repository

import com.miassolutions.milkledger.data.local.daos.PurchaseDao
import com.miassolutions.milkledger.data.local.daos.TransactionDao
import com.miassolutions.milkledger.data.local.entities.TransactionEntity
import com.miassolutions.milkledger.data.local.entities.TransactionType
import com.miassolutions.milkledger.data.mapper.toDomain
import com.miassolutions.milkledger.data.mapper.toEntity
import com.miassolutions.milkledger.domain.model.Purchase
import com.miassolutions.milkledger.domain.model.Supplier
import com.miassolutions.milkledger.domain.model.Transaction
import com.miassolutions.milkledger.presentation.stats.SupplierPaidSummary
import com.miassolutions.milkledger.utils.extensions.toMillis
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PurchaseRepository @Inject constructor(
    private val purchaseDao: PurchaseDao,
    private val transactionDao: TransactionDao,
) {

    companion object {
        private const val TAG = "PurchaseRepository"
        private const val COLLECTION = "purchases"
    }

    /* ---------------------------------------------------
       READ
    --------------------------------------------------- */

    fun getPaidToSuppliersForDate(date: LocalDate): Flow<List<SupplierPaidSummary>> =
        purchaseDao.getPaidAmountToSupplierForDate(date.toMillis())

    fun observeSuppliersList(): Flow<List<Supplier>> =
        purchaseDao.observeSuppliersList()
            .map { it.map { s -> s.toDomain() } }

    fun getPurchasesByDate(date: LocalDate): Flow<List<Purchase>> =
        purchaseDao.getPurchasesByDate(date.toMillis())
            .map { list -> list.map { it.purchase.toDomain() } }

    fun getPurchasesForSupplier(supplierId: String): Flow<List<Purchase>> =
        purchaseDao.getPurchasesForSupplier(supplierId)
            .map { list -> list.map { it.purchase.toDomain() } }

    suspend fun isDuplicatePurchase(supplierId: String, date: LocalDate): Boolean =
        purchaseDao.countPurchaseForDate(supplierId, date.toMillis()) > 0



    /* ---------------------------------------------------
       WRITE : PURCHASE
    --------------------------------------------------- */

    suspend fun insertPurchase(purchase: Purchase) {
        val entity = purchase.toEntity()

        // 1️⃣ Save purchase
        purchaseDao.insertPurchase(entity)

        // 2️⃣ Ledger entry (PURCHASE = money OUT)
        transactionDao.insert(
            TransactionEntity(
                dateMillis = entity.dateMillis,
                type = TransactionType.PURCHASE,   // 🆕
                referenceId = entity.purchaseId,
                accountId = entity.supplierId,
                debit = entity.payment,
                credit = 0.0,
                profitImpact = -entity.payment,
                notes = "Purchase from supplier ${entity.supplierId}"
            )
        )


    }

    suspend fun updatePurchase(purchase: Purchase) {
        val entity = purchase.toEntity()

        purchaseDao.updatePurchase(entity)

        // Append-only ledger
        transactionDao.insert(
            TransactionEntity(
                dateMillis = entity.dateMillis,
                type = TransactionType.PURCHASE,
                referenceId = entity.purchaseId,
                accountId = entity.supplierId,
                debit = entity.payment,
                credit = 0.0,
                profitImpact = -entity.payment,
                notes = "Purchase updated"
            )
        )
    }

    suspend fun deletePurchase(purchaseId: String) {
        val deletedAt = System.currentTimeMillis()

        purchaseDao.softDeletePurchase(purchaseId, deletedAt)

        // 🆕 Ledger reversal
//        transactionDao.insert(
//            TransactionEntity(
//                dateMillis = deletedAt,
//                type = TransactionType.PURCHASE_REVERSAL,
//                referenceId = purchaseId,
//                debit = 0.0,
//                credit = 0.0,
//                profitImpact = 0.0,
//                notes = "Purchase deleted"
//            )
//        )

    }

    /* ---------------------------------------------------
       ADJUSTMENTS
    --------------------------------------------------- */

    suspend fun addPurchaseAdjustment(
        purchaseId: String,
        amount: Double,
        note: String?
    ) {
        if (amount == 0.0) return
        if (purchaseDao.countPurchaseById(purchaseId) == 0) return

//        transactionDao.insert(
//            TransactionEntity(
//                dateMillis = System.currentTimeMillis(),
//                type = TransactionType.PROFIT_ADJUSTMENT,
//                referenceId = purchaseId,
//                accountId = customerId,
//                debit = if (amount > 0) amount else 0.0,
//                credit = if (amount < 0) -amount else 0.0,
//                profitImpact = -amount,
//                notes = note
//            )
//        )
    }

    fun observePurchaseAdjustments(purchaseId: String): Flow<List<Transaction>> =
        transactionDao.getAdjustmentsFor(purchaseId)
            .map { it.map { tx -> tx.toDomain() } }


}

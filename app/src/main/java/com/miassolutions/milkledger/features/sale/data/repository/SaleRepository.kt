package com.miassolutions.milkledger.features.sale.data.repository

import com.miassolutions.milkledger.features.sale.data.local.SaleDao
import com.miassolutions.milkledger.features.sale.data.local.SaleEntity
import com.miassolutions.milkledger.features.sale.mapper.toDomain
import com.miassolutions.milkledger.features.sale.mapper.toEntity
import com.miassolutions.milkledger.features.sale.domain.model.Sale
import com.miassolutions.milkledger.features.transaction.data.TransactionDao
import com.miassolutions.milkledger.features.transaction.data.TransactionEntity
import com.miassolutions.milkledger.features.transaction.data.TransactionType
import com.miassolutions.milkledger.utils.extensions.toMillis
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.Boolean
import kotlin.Double
import kotlin.String
import kotlin.collections.List
import kotlin.collections.map

@Singleton
class SaleRepository @Inject constructor(
    private val saleDao: SaleDao,
    private val transactionDao: TransactionDao,

    ) {


    /* ---------------------------------------------------
       CUSTOMER BALANCE HISTORY
    --------------------------------------------------- */

//    suspend fun getCustomerBalanceHistory(
//        customerId: String
//    ): List<BalanceHistoryItem> {
//
//        val ledger = transactionDao.customerLedger(customerId)
//
//        var runningBalance = 0.0
//
//        return ledger.map { row ->
//
//            val delta = row.credit - row.debit
//            runningBalance += delta
//
//            BalanceHistoryItem(
//                date = row.dateMillis.toLocalDate(),
//                change = delta,
//                balanceAfter = runningBalance,
//                note = row.note
//            )
//        }.reversed()
//    }

    /* ---------------------------------------------------
       READ
    --------------------------------------------------- */

    suspend fun getSaleById(saleId: String): Sale? {
        val entity = saleDao.getSaleById(saleId)
        return entity?.toDomain()
    }

    fun getSalesByDate(date: LocalDate): Flow<List<Sale>> =
        saleDao.getSalesByDate(date.toMillis())
            .map { list -> list.map { it.sale.toDomain() } }

//    fun getSalesForCustomer(customerId: String): Flow<List<Sale>> =
//        salesDao.getSalesForCustomer(customerId)
//            .map { list -> list.map { it.sale.toDomain() } }

    suspend fun isDuplicateSale(
        customerId: String,
        date: LocalDate
    ): Boolean =
        saleDao.countSalesForDate(customerId, date.toMillis()) > 0

    /* ---------------------------------------------------
       WRITE : SALE
    --------------------------------------------------- */

    suspend fun insertSale(sale: Sale) {
        val entity = sale.toEntity()

        // 1️⃣ Save sale
        saleDao.insertSale(entity)

        // 2️⃣ Ledger entry (SALE)
        transactionDao.insert(
            TransactionEntity(
                dateMillis = entity.dateMillis,
                type = TransactionType.SALE,
                referenceId = entity.saleId,
                accountId = entity.customerId,      // 🔥 REQUIRED
                debit = 0.0,
                credit = entity.paid,
                profitImpact = entity.paid,
                note = "Sale created"
            )
        )

    }

    suspend fun updateSale(updated: Sale) {

        val entity = updated.toEntity()

        // 1️⃣ Get previous sale (needed for reversal)
        val old = saleDao.getSaleById(entity.saleId) ?: return

        // 2️⃣ Update sale row
        saleDao.updateSale(entity)

        // 3️⃣ Reverse previous ledger impact
        transactionDao.insert(
            TransactionEntity(
                dateMillis = System.currentTimeMillis(),
                type = TransactionType.SALE_REVERSAL,
                referenceId = entity.saleId,
                accountId = old.customerId,
                debit = old.paid,
                credit = 0.0,
                profitImpact = -old.paid,
                note = "Sale updated (reversal)"
            )
        )

        // 4️⃣ Insert updated ledger entry
        transactionDao.insert(
            TransactionEntity(
                dateMillis = entity.dateMillis,
                type = TransactionType.SALE,
                referenceId = entity.saleId,
                accountId = entity.customerId,
                debit = 0.0,
                credit = entity.paid,
                profitImpact = entity.paid,
                note = "Sale updated"
            )
        )
    }

    suspend fun deleteSale(saleId: String) {

        val deletedAt = System.currentTimeMillis()

        // 1️⃣ Fetch sale before deletion
        val sale = saleDao.getSaleById(saleId) ?: return

        // 2️⃣ Soft delete sale
        saleDao.softDeleteSale(saleId, deletedAt)

        // 3️⃣ Ledger reversal
        transactionDao.insert(
            TransactionEntity(
                dateMillis = deletedAt,
                type = TransactionType.SALE_REVERSAL,
                referenceId = sale.saleId,
                accountId = sale.customerId,
                debit = sale.paid,
                credit = 0.0,
                profitImpact = -sale.paid,
                note = "Sale deleted"
            )
        )

    }

    /* ---------------------------------------------------
       ADJUSTMENTS
    --------------------------------------------------- */

    suspend fun addSaleAdjustment(
        saleId: String,
        customerId: String,
        amount: Double,
        note: String?
    ) {
        if (amount == 0.0) return

        transactionDao.insert(
            TransactionEntity(
                dateMillis = System.currentTimeMillis(),
                type = TransactionType.PROFIT_ADJUSTMENT,
                referenceId = saleId,
                accountId = customerId,
                debit = if (amount < 0) -amount else 0.0,
                credit = if (amount > 0) amount else 0.0,
                profitImpact = amount,
                note = note
            )
        )
    }


}
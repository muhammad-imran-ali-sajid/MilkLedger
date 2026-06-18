package com.miassolutions.milkledger.features.purchase.data

import androidx.room.Transaction
import androidx.room.withTransaction
import com.miassolutions.milkledger.core.localdb.account.local.AccountDao
import com.miassolutions.milkledger.core.localdb.account.local.AccountType
import com.miassolutions.milkledger.core.localdb.account.local.toDomain
import com.miassolutions.milkledger.core.localdb.database.AppDatabase
import com.miassolutions.milkledger.core.localdb.ledger.LedgerDao
import com.miassolutions.milkledger.core.localdb.ledger.LedgerEntryType
import com.miassolutions.milkledger.core.localdb.milk.MilkDao
import com.miassolutions.milkledger.features.account.domain.Account
import com.miassolutions.milkledger.features.backup.data.BackupRepository
import com.miassolutions.milkledger.features.purchase.model.MilkPurchaseUiModel
import com.miassolutions.milkledger.features.purchase.model.PurchaseSummary
import com.miassolutions.milkledger.features.purchase.model.UpdatePurchaseRequest
import com.miassolutions.milkledger.utils.extensions.toMillis
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject

class MilkPurchaseRepository @Inject constructor(
    private val accountDao: AccountDao,
    private val milkDao: MilkDao,
    private val ledgerDao: LedgerDao,
    private val backupRepository: BackupRepository,
    private val db: AppDatabase,
    private val purchaseLedgerFactory: PurchaseLedgerFactory = PurchaseLedgerFactory()
) {

    fun getSupplierSummary(
        supplierId: String,
        start: Long,
        end: Long
    ): Flow<PurchaseSummary> {
        return milkDao.getSupplierSummary(supplierId, start, end)
    }

    fun getSupplierHistory(
        supplierId: String,
        start: Long,
        end: Long
    ): Flow<List<MilkPurchaseUiModel>> {
        return milkDao.getSupplierHistory(supplierId, start, end)
    }

    fun getGlobalPurchaseStats(start: Long, end: Long): Flow<PurchaseSummary> =
        milkDao.getGlobalPurchaseStats(start, end)

    fun getSuppliers(): Flow<List<Account>> {
        return accountDao.getAccountsByType(AccountType.SUPPLIER).map { list ->
            list.filter { it.isActive }.map { it.toDomain() }
        }
    }

    fun getSuppliersWithPurchaseOnDate(date: Long): Flow<List<String>> =
        milkDao.getSuppliersWithPurchaseOnDate(date)

    fun getPurchasesByDate(date: Long): Flow<List<MilkPurchaseUiModel>> =
        milkDao.getPurchasesByDate(date)

    fun getAccountBalance(accountId: String): Flow<Long> {
        return ledgerDao.getAccountBalance(accountId)
    }

    suspend fun getPurchaseById(id: String): MilkPurchaseUiModel? {
        return milkDao.getPurchaseDetailById(id)
    }

    @Transaction
    suspend fun deletePurchase(purchaseId: String) {
        val currentTime = System.currentTimeMillis()
        db.withTransaction {
            milkDao.softDeleteMilkTransaction(purchaseId, currentTime)
            ledgerDao.softDeleteLedgerByReference(purchaseId, currentTime)
        }
        backupRepository.markDataChanged()
    }

    suspend fun saveMilkPurchase(
        supplierId: String,
        date: LocalDate,
        paymentDate: LocalDate?,
        volume: Double,
        fat: Double,
        lr: Double,
        rate: Double,
        amountPaid: Long,
        note: String?
    ) {
        db.withTransaction {
            val amounts = purchaseLedgerFactory.calculateAmounts(volume, fat, lr, rate)
            val supplierName = accountDao.getAccountById(supplierId)?.name ?: "Supplier"

            val milkEntity = purchaseLedgerFactory.createMilkTransaction(
                supplierId = supplierId,
                date = date,
                paymentDate = paymentDate,
                volume = volume,
                fat = fat,
                lr = lr,
                ts = amounts.ts,
                rate = rate,
                totalPricePaisa = amounts.totalPricePaisa,
                note = note
            )
            milkDao.insert(milkEntity)

            ledgerDao.insert(
                purchaseLedgerFactory.createPurchaseLedger(
                    date = date,
                    supplierId = supplierId,
                    purchaseId = milkEntity.milkTransId,
                    volume = volume,
                    fat = fat,
                    lr = lr,
                    totalPricePaisa = amounts.totalPricePaisa
                )
            )

            if (amountPaid > 0) {
                val paymentNote =
                    purchaseLedgerFactory.paymentNoteForSave(supplierName, date, paymentDate)

                ledgerDao.insert(
                    purchaseLedgerFactory.createCashPaidLedger(
                        date = date,
                        supplierId = supplierId,
                        purchaseId = milkEntity.milkTransId,
                        amountPaid = amountPaid,
                        note = paymentNote
                    )
                )
            }
        }
        backupRepository.markDataChanged()
    }

    suspend fun updateMilkPurchase(request: UpdatePurchaseRequest) {
        db.withTransaction {
            val amounts = purchaseLedgerFactory.calculateAmounts(
                request.volume,
                request.fat,
                request.lr,
                request.rate
            )
            val supplierName = accountDao.getAccountById(request.supplierId)?.name ?: "Supplier"

            val oldPurchase = milkDao.getMilkTransactionById(request.purchaseId) ?: throw Exception(
                "Purchase not found"
            )

            val updatedMilk = oldPurchase.copy(
                dateMillis = request.date.toMillis(),
                paymentDateMillis = request.paymentDate?.toMillis(),
                volume = request.volume,
                fat = request.fat,
                lr = request.lr,
                ts = amounts.ts,
                quantity = request.volume,
                rateUsed = request.rate,
                totalAmount = amounts.totalPricePaisa,
                notes = request.note,
                updatedAtMillis = System.currentTimeMillis()
            )
            milkDao.update(updatedMilk)

            val purchaseLedger =
                ledgerDao.getLedgerByReferenceId(request.purchaseId, LedgerEntryType.MILK_PURCHASE)
            purchaseLedger?.let {
                ledgerDao.update(
                    it.copy(
                        dateMillis = request.date.toMillis(),
                        credit = amounts.totalPricePaisa,
                        profitImpact = -amounts.totalPricePaisa,
                        note = purchaseLedgerFactory.purchaseNote(
                            request.volume,
                            request.fat,
                            request.lr
                        ),
                        updatedAtMillis = System.currentTimeMillis()
                    )
                )
            }

            val paymentLedger =
                ledgerDao.getLedgerByReferenceId(request.purchaseId, LedgerEntryType.CASH_PAID)

            if (request.amountPaid > 0) {
                val paymentNote = purchaseLedgerFactory.paymentNoteForUpdate(
                    supplierName,
                    request.date,
                    request.paymentDate
                )

                if (paymentLedger != null) {
                    ledgerDao.update(
                        paymentLedger.copy(
                            dateMillis = request.date.toMillis(),
                            debit = request.amountPaid,
                            note = paymentNote,
                            updatedAtMillis = System.currentTimeMillis()
                        )
                    )
                } else {
                    ledgerDao.insert(
                        purchaseLedgerFactory.createCashPaidLedger(
                            date = request.date,
                            supplierId = request.supplierId,
                            purchaseId = request.purchaseId,
                            amountPaid = request.amountPaid,
                            note = paymentNote
                        )
                    )
                }
            } else {
                if (paymentLedger != null) ledgerDao.delete(paymentLedger)
            }
        }

        backupRepository.markDataChanged()
    }
}

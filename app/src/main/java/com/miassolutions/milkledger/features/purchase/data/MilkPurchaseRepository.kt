package com.miassolutions.milkledger.features.purchase.data


import androidx.room.Transaction
import androidx.room.withTransaction
import com.miassolutions.milkledger.core.localdb.AppDatabase
import com.miassolutions.milkledger.core.localdb.account.local.AccountDao
import com.miassolutions.milkledger.core.localdb.account.local.AccountType
import com.miassolutions.milkledger.core.localdb.account.local.toDomain
import com.miassolutions.milkledger.core.localdb.ledger.FinancialLedgerEntity
import com.miassolutions.milkledger.core.localdb.ledger.LedgerDao
import com.miassolutions.milkledger.core.localdb.ledger.LedgerEntryType
import com.miassolutions.milkledger.core.localdb.milk.MilkDao
import com.miassolutions.milkledger.core.localdb.milk.MilkTransactionEntity
import com.miassolutions.milkledger.core.localdb.milk.TransactionType
import com.miassolutions.milkledger.features.account.domain.Account
import com.miassolutions.milkledger.features.purchase.model.MilkPurchaseUiModel
import com.miassolutions.milkledger.features.purchase.model.PurchaseSummary
import com.miassolutions.milkledger.features.purchase.model.UpdatePurchaseRequest
import com.miassolutions.milkledger.utils.extensions.toDisplayDate
import com.miassolutions.milkledger.utils.extensions.toLongPaisa
import com.miassolutions.milkledger.utils.extensions.toMillis
import com.miassolutions.milkledger.utils.milkcalculations.MilkCalculationUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class MilkPurchaseRepository @Inject constructor(
    private val accountDao: AccountDao,
    private val milkDao: MilkDao,
    private val ledgerDao: LedgerDao,
    private val db: AppDatabase
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
        // Sirf Active accounts layen
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
        milkDao.softDeleteMilkTransaction(purchaseId, currentTime)
        ledgerDao.softDeleteLedgerByReference(purchaseId, currentTime)
    }


    // ✅ SAVE PURCHASE
    suspend fun saveMilkPurchase(
        supplierId: String,
        date: LocalDate,        // Accounting Date
        paymentDate: LocalDate?,// UI Reference Date (Nullable)
        volume: Double,
        fat: Double,
        lr: Double,
        rate: Double,
        amountPaid: Long,
        note: String?
    ) {
        db.withTransaction {
            // Calculations
            val ts = MilkCalculationUtils.calculateTS(fat, lr, volume)
            val totalPricePaisa =
                MilkCalculationUtils.calculatePrice(volume, fat, lr, rate).toLongPaisa()
            val supplierName = accountDao.getAccountById(supplierId)?.name ?: "Supplier"

            // 1. Save Milk Entity
            val milkEntity = MilkTransactionEntity(
                accountId = supplierId,
                dateMillis = date.toMillis(),

                // 🔥 NEW: Save User Selected Date here (Reference k liye)
                paymentDateMillis = paymentDate?.toMillis(),

                type = TransactionType.PURCHASE,
                volume = volume, fat = fat, lr = lr, ts = ts, quantity = volume, deduction = 0.0,
                rateUsed = rate, totalAmount = totalPricePaisa, notes = note
            )
            milkDao.insert(milkEntity)

            // 2. Ledger Entry: MILK_PURCHASE
            ledgerDao.insert(
                FinancialLedgerEntity(
                    dateMillis = date.toMillis(), // Purchase Date
                    accountId = supplierId,
                    referenceId = milkEntity.milkTransId,
                    type = LedgerEntryType.MILK_PURCHASE,
                    debit = 0, credit = totalPricePaisa, profitImpact = -totalPricePaisa,
                    note = "Purchase: $volume Ltr (F:$fat, L:$lr)"
                )
            )

            // 3. Ledger Entry: CASH_PAID
            if (amountPaid > 0) {
                // UI Note Logic
                val finalNote = if (paymentDate != null && !date.isEqual(paymentDate)) {
                    "Paid to $supplierName\n(Dated: ${paymentDate.toDisplayDate()})"
                } else {
                    "Paid to $supplierName"
                }

                ledgerDao.insert(
                    FinancialLedgerEntity(
                        // 🟢 LEDGER: Purchase Date par hi rahega (Accounting Rule)
                        dateMillis = date.toMillis(),

                        accountId = supplierId,
                        type = LedgerEntryType.CASH_PAID,
                        referenceId = milkEntity.milkTransId,
                        debit = amountPaid,
                        credit = 0,
                        profitImpact = 0,
                        note = finalNote
                    ))
            }
        }
    }

    // ✅ UPDATE PURCHASE
    suspend fun updateMilkPurchase(request: UpdatePurchaseRequest) {
        db.withTransaction {
            val ts = MilkCalculationUtils.calculateTS(request.fat, request.lr, request.volume)
            val totalPricePaisa = MilkCalculationUtils.calculatePrice(
                request.volume,
                request.fat,
                request.lr,
                request.rate
            ).toLongPaisa()
            val supplierName = accountDao.getAccountById(request.supplierId)?.name ?: "Supplier"

            val oldPurchase = milkDao.getMilkTransactionById(request.purchaseId) ?: throw Exception(
                "Purchase not found"
            )

            // 1. Update Milk Entity
            val updatedMilk = oldPurchase.copy(
                dateMillis = request.date.toMillis(),

                // 🔥 NEW: Update User Selected Date
                paymentDateMillis = request.paymentDate?.toMillis(),

                volume = request.volume,
                fat = request.fat,
                lr = request.lr,
                ts = ts,
                quantity = request.volume,
                rateUsed = request.rate,
                totalAmount = totalPricePaisa,
                notes = request.note,
                updatedAtMillis = System.currentTimeMillis()
            )
            milkDao.update(updatedMilk)

            // 2. Update Ledger (Purchase Entry)
            val purchaseLedger =
                ledgerDao.getLedgerByReferenceId(request.purchaseId, LedgerEntryType.MILK_PURCHASE)
            purchaseLedger?.let {
                ledgerDao.update(
                    it.copy(
                        dateMillis = request.date.toMillis(),
                        credit = totalPricePaisa, profitImpact = -totalPricePaisa,
                        note = "Purchase: ${request.volume} Ltr (F:${request.fat}, L:${request.lr})",
                        updatedAtMillis = System.currentTimeMillis()
                    )
                )
            }

            // 3. Update Payment Ledger
            val paymentLedger =
                ledgerDao.getLedgerByReferenceId(request.purchaseId, LedgerEntryType.CASH_PAID)

            if (request.amountPaid > 0) {
                val finalNote =
                    if (!request.date.isEqual(request.paymentDate)) {
                        "Paid to $supplierName\n(Dated: ${request.paymentDate?.toDisplayDate()})"
                    } else {
                        "Paid to $supplierName"
                    }

                if (paymentLedger != null) {
                    // Update Existing
                    ledgerDao.update(
                        paymentLedger.copy(
                            // 🟢 LEDGER: Purchase Date par hi lock rahega
                            dateMillis = request.date.toMillis(),
                            debit = request.amountPaid,
                            note = finalNote,
                            updatedAtMillis = System.currentTimeMillis()
                        )
                    )
                } else {
                    // Insert New
                    ledgerDao.insert(
                        FinancialLedgerEntity(
                            // 🟢 LEDGER: Purchase Date
                            dateMillis = request.date.toMillis(),
                            accountId = request.supplierId,
                            type = LedgerEntryType.CASH_PAID,
                            referenceId = request.purchaseId,
                            debit = request.amountPaid, credit = 0, profitImpact = 0,
                            note = finalNote
                        )
                    )
                }
            } else {
                if (paymentLedger != null) ledgerDao.delete(paymentLedger)
            }
        }
    }

}
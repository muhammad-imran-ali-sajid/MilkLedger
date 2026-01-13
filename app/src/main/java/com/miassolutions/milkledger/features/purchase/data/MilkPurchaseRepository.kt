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
import com.miassolutions.milkledger.features.purchase.model.SupplierDropDownUiModel
import com.miassolutions.milkledger.features.purchase.model.UpdatePurchaseRequest
import com.miassolutions.milkledger.utils.extensions.toLongPaisa
import com.miassolutions.milkledger.utils.extensions.toMillis
import com.miassolutions.milkledger.utils.milkcalculations.MilkCalculationUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject

class MilkPurchaseRepository @Inject constructor(
    private val accountDao: AccountDao,
    private val milkDao: MilkDao,
    private val ledgerDao: LedgerDao,
    private val db: AppDatabase
) {

    fun getSupplierHistory(
        supplierId: String,
        start: Long,
        end: Long
    ): Flow<List<MilkPurchaseUiModel>> {
        return milkDao.getSupplierHistory(supplierId, start, end)
    }

    fun getAccountLedgerHistory(accountId: String) = ledgerDao.getLedgerHistory(accountId)
//    fun getSuppliers(): Flow<List<Account>> =
//        accountDao.getAccountsByType(AccountType.SUPPLIER).map { list ->
//            list.map { it.toDomain() }
//        }

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
    suspend fun deletePurchase(saleId: String) {
        val currentTime = System.currentTimeMillis()
        milkDao.softDeleteMilkTransaction(saleId, currentTime)
        ledgerDao.softDeleteLedgerByReference(saleId, currentTime)
    }

    suspend fun saveMilkPurchase(
        supplierId: String,
        date: LocalDate,
        paymentDate: LocalDate,
        volume: Double,
        fat: Double,
        lr: Double,
        rate: Double,
        amountPaid: Long,
        note: String?
    ) {
        db.withTransaction {
            // 1. Calculations
            val ts = MilkCalculationUtils.calculateTS(fat, lr, volume)
            val totalPriceDouble = MilkCalculationUtils.calculatePrice(volume, fat, lr, rate)
            val totalPricePaisa = totalPriceDouble.toLongPaisa()

            // 🔥 NEW: Fetch Supplier Name for Note
            val supplierAccount = accountDao.getAccountById(supplierId)
            val supplierName = supplierAccount?.name ?: "Supplier"

            // 2. Save Milk Entity (PURCHASE)
            val milkEntity = MilkTransactionEntity(
                accountId = supplierId,
                dateMillis = date.toMillis(),
                type = TransactionType.PURCHASE,
                volume = volume,
                fat = fat,
                lr = lr,
                ts = ts,
                quantity = volume,
                deduction = 0.0,
                rateUsed = rate,
                totalAmount = totalPricePaisa,
                notes = note
            )
            milkDao.insert(milkEntity)

            // 3. Ledger Entry: MILK_PURCHASE (Credit)
            val purchaseLedger = FinancialLedgerEntity(
                dateMillis = date.toMillis(),
                accountId = supplierId,
                referenceId = milkEntity.milkTransId,
                type = LedgerEntryType.MILK_PURCHASE,
                debit = 0,
                credit = totalPricePaisa,
                profitImpact = -totalPricePaisa,

                // Purchase Note: "Purchase: 50L (Fat: 5.0)"
                note = "Purchase: $volume Ltr (F:$fat, L:$lr)"
            )
            ledgerDao.insert(purchaseLedger)

            // 4. Ledger Entry: CASH_PAID (Debit)
            if (amountPaid > 0) {
                val paymentLedger = FinancialLedgerEntity(
                    dateMillis = paymentDate.toMillis(),
                    accountId = supplierId,
                    type = LedgerEntryType.CASH_PAID,
                    referenceId = milkEntity.milkTransId,
                    debit = amountPaid,
                    credit = 0,
                    profitImpact = 0,

                    // 🔥 UPDATED NOTE: Ab CashFlow me naam show hoga
                    note = "Paid to $supplierName"
                )
                ledgerDao.insert(paymentLedger)
            }
        }
    }

    suspend fun updateMilkPurchase(request: UpdatePurchaseRequest) {
        db.withTransaction {
            // 1. Re-Calculate
            val ts = MilkCalculationUtils.calculateTS(request.fat, request.lr, request.volume)
            val totalPriceDouble = MilkCalculationUtils.calculatePrice(
                request.volume,
                request.fat,
                request.lr,
                request.rate
            )
            val totalPricePaisa = totalPriceDouble.toLongPaisa()

            // 🔥 NEW: Fetch Name again (in case supplier changed or just for record)
            val supplierAccount = accountDao.getAccountById(request.supplierId)
            val supplierName = supplierAccount?.name ?: "Supplier"

            val oldPurchase = milkDao.getMilkTransactionById(request.purchaseId)
                ?: throw Exception("Purchase not found")

            // 2. Update Milk Table
            val updatedMilk = oldPurchase.copy(
                dateMillis = request.date.toMillis(),
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

            // 3. Update Ledger (Purchase Entry)
            val purchaseLedger =
                ledgerDao.getLedgerByReferenceId(request.purchaseId, LedgerEntryType.MILK_PURCHASE)
            purchaseLedger?.let {
                val updated = it.copy(
                    dateMillis = request.date.toMillis(),
                    credit = totalPricePaisa,
                    profitImpact = -totalPricePaisa,
                    note = "Purchase: ${request.volume} Ltr (F:${request.fat}, L:${request.lr})",
                    updatedAtMillis = System.currentTimeMillis()
                )
                ledgerDao.update(updated)
            }

            // 4. Update Payment (Debit)
            val paymentLedger =
                ledgerDao.getLedgerByReferenceId(request.purchaseId, LedgerEntryType.CASH_PAID)

            if (paymentLedger != null) {
                if (request.amountPaid > 0) {
                    // Update
                    ledgerDao.update(
                        paymentLedger.copy(
                            dateMillis = request.paymentDate.toMillis(),
                            debit = request.amountPaid,

                            // 🔥 UPDATED NOTE
                            note = "Paid to $supplierName",

                            updatedAtMillis = System.currentTimeMillis()
                        )
                    )
                } else {
                    // Delete
                    ledgerDao.delete(paymentLedger)
                }
            } else if (request.amountPaid > 0) {
                // Insert New
                ledgerDao.insert(
                    FinancialLedgerEntity(
                        dateMillis = request.paymentDate.toMillis(),
                        accountId = request.supplierId,
                        type = LedgerEntryType.CASH_PAID,
                        referenceId = request.purchaseId,
                        debit = request.amountPaid,
                        credit = 0,
                        profitImpact = 0,

                        // 🔥 NEW NOTE
                        note = "Paid to $supplierName"
                    )
                )
            }
        }
    }



}
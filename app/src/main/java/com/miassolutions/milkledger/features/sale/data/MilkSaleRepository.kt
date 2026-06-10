package com.miassolutions.milkledger.features.sale.data

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
import com.miassolutions.milkledger.features.backup.data.BackupRepository
import com.miassolutions.milkledger.features.purchase.model.SaleSummary
import com.miassolutions.milkledger.features.sale.model.MilkSaleUiModel
import com.miassolutions.milkledger.features.sale.model.UpdateSaleRequest
import com.miassolutions.milkledger.utils.extensions.toDisplayDate
import com.miassolutions.milkledger.utils.extensions.toLongPaisa
import com.miassolutions.milkledger.utils.extensions.toMillis
import com.miassolutions.milkledger.utils.milkcalculations.MilkCalculationUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class MilkSaleRepository @Inject constructor(
    private val milkDao: MilkDao,
    private val ledgerDao: LedgerDao,
    private val accountDao: AccountDao,
    private val backupRepository: BackupRepository,
    private val db: AppDatabase
) {
    
    // ... (Getters bilkul theek hain) ...
    
    fun getCustomerSummary(accountId: String, start: Long, end: Long) =
        milkDao.getCustomerSummary(accountId, start, end)
    
    fun getCustomerHistory(accountId: String, start: Long, end: Long) =
        milkDao.getCustomerSalesHistory(accountId, start, end)
    
    fun getSalesByDate(date: Long) = milkDao.getMilkSalesByDate(date)
    fun getAccountBalance(accountId: String) = ledgerDao.getAccountBalance(accountId)
    fun getGlobalSaleStats(start: Long, end: Long) = milkDao.getGlobalSaleStats(start, end)
    suspend fun getSaleById(id: String) = milkDao.getSaleDetailById(id)
    
    fun getCustomers(): Flow<List<Account>> {
        return accountDao.getAccountsByType(AccountType.CUSTOMER).map { list ->
            list.filter { it.isActive }.map { it.toDomain() }
        }
    }
    
    @Transaction
    suspend fun deleteSale(saleId: String) {
        val currentTime = System.currentTimeMillis()
        
        db.withTransaction {
            milkDao.softDeleteMilkTransaction(saleId, currentTime)
            ledgerDao.softDeleteLedgerByReference(saleId, currentTime)
        }
        backupRepository.markDataChanged()
        
    }
    
    
    // ✅ SAVE SALE
    suspend fun saveMilkSale(
        saleDate: LocalDate,
        paymentDate: LocalDate?, // UI Reference Date (Nullable)
        accountId: String,
        volume: Double,
        deduction: Double,
        rate: Double,
        amountPaid: Long,
        note: String?
    ) {
        db.withTransaction {
            val customerName = accountDao.getAccountById(accountId)?.name ?: "Unknown Customer"
            
            val netQuantity = volume - deduction
            val totalPricePaisa =
                MilkCalculationUtils.calculateCustomerPrice(volume, deduction, rate).toLongPaisa()
            
            // 1. Save Milk (Use Sale Date for Ledger, PaymentDate for UI)
            val milkEntity = MilkTransactionEntity(
                accountId = accountId,
                dateMillis = saleDate.toMillis(),
                
                // 🔥 NEW: Save User Selected Date
                paymentDateMillis = paymentDate?.toMillis(),
                
                type = TransactionType.SALE,
                volume = volume, deduction = deduction, quantity = netQuantity,
                rateUsed = rate, totalAmount = totalPricePaisa, notes = note
            )
            milkDao.insert(milkEntity)
            
            // 2. Save Ledger Debit (Bill)
            ledgerDao.insert(
                FinancialLedgerEntity(
                    dateMillis = saleDate.toMillis(),
                    accountId = accountId,
                    referenceId = milkEntity.milkTransId,
                    type = LedgerEntryType.MILK_SALE,
                    debit = totalPricePaisa, credit = 0, profitImpact = totalPricePaisa,
                    note = "Sale: $volume - $deduction = $netQuantity L"
                )
            )
            
            // 3. Save Payment (Cash Received)
            if (amountPaid > 0) {
                // 🔥 Fix: Null Safe check
                val finalNote = if (paymentDate != null && !saleDate.isEqual(paymentDate)) {
                    "$customerName\n(Dated: ${paymentDate.toDisplayDate()})"
                } else {
                    "$customerName"
                }
                
                ledgerDao.insert(
                    FinancialLedgerEntity(
                        // 🟢 LEDGER: Sale Date par hi rahega
                        dateMillis = saleDate.toMillis(),
                        
                        accountId = accountId,
                        type = LedgerEntryType.CASH_RECEIVED,
                        referenceId = milkEntity.milkTransId,
                        debit = 0, credit = amountPaid, profitImpact = 0,
                        note = finalNote
                    )
                )
            }
        }
        backupRepository.markDataChanged()
    }
    
    // ✅ UPDATE SALE
    suspend fun updateMilkSale(request: UpdateSaleRequest) {
        db.withTransaction {
            val netQuantity = request.volume - request.deduction
            val totalPricePaisa = MilkCalculationUtils.calculateCustomerPrice(
                request.volume,
                request.deduction,
                request.rate
            ).toLongPaisa()
            
            val oldSale =
                milkDao.getMilkTransactionById(request.saleId) ?: throw Exception("Sale not found")
            val customerName =
                accountDao.getAccountById(oldSale.accountId)?.name ?: "Unknown Customer"
            
            // 1. Update Milk Entity
            val updatedMilkEntity = oldSale.copy(
                dateMillis = request.date.toMillis(),
                
                // 🔥 NEW: Update User Selected Date
                paymentDateMillis = request.paymentDate?.toMillis(),
                
                volume = request.volume, deduction = request.deduction, quantity = netQuantity,
                rateUsed = request.rate, totalAmount = totalPricePaisa, notes = request.note,
                updatedAtMillis = System.currentTimeMillis()
            )
            milkDao.update(updatedMilkEntity)
            
            // 2. Update Ledger Debit
            val saleLedgerEntry =
                ledgerDao.getLedgerByReferenceId(request.saleId, LedgerEntryType.MILK_SALE)
            saleLedgerEntry?.let { entry ->
                ledgerDao.update(
                    entry.copy(
                        dateMillis = request.date.toMillis(),
                        debit = totalPricePaisa, profitImpact = totalPricePaisa,
                        note = "Sale: ${request.volume} - ${request.deduction} = $netQuantity L",
                        updatedAtMillis = System.currentTimeMillis()
                    )
                )
            }
            
            // 3. Update Payment (Cash Received)
            val paymentLedgerEntry =
                ledgerDao.getLedgerByReferenceId(request.saleId, LedgerEntryType.CASH_RECEIVED)
            
            if (request.amountPaid > 0) {
                // 🔥 Fix: Null Safe check
                val finalNote =
                    if (request.paymentDate != null && !request.date.isEqual(request.paymentDate)) {
                        "$customerName\n(Dated: ${request.paymentDate.toDisplayDate()})"
                    } else {
                        "$customerName"
                    }
                
                if (paymentLedgerEntry != null) {
                    // Update Existing
                    ledgerDao.update(
                        paymentLedgerEntry.copy(
                            // 🟢 LEDGER: Sale Date par hi lock rahega
                            dateMillis = request.date.toMillis(),
                            credit = request.amountPaid,
                            note = finalNote,
                            updatedAtMillis = System.currentTimeMillis()
                        )
                    )
                } else {
                    // Insert New (Use Sale Date)
                    ledgerDao.insert(
                        FinancialLedgerEntity(
                            dateMillis = request.date.toMillis(), // Ledger Date
                            accountId = request.accountId,
                            type = LedgerEntryType.CASH_RECEIVED,
                            referenceId = request.saleId,
                            debit = 0, credit = request.amountPaid, profitImpact = 0,
                            note = finalNote
                        )
                    )
                }
            } else {
                if (paymentLedgerEntry != null) ledgerDao.delete(paymentLedgerEntry)
            }
        }
        backupRepository.markDataChanged()
    }
}
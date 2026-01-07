package com.miassolutions.milkledger.features.sale.data // Package verify kr len

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
import com.miassolutions.milkledger.features.sale.model.MilkSaleUiModel
import com.miassolutions.milkledger.features.sale.model.UpdateSaleRequest
import com.miassolutions.milkledger.utils.extensions.toLongPaisa
import com.miassolutions.milkledger.utils.extensions.toMillis
import com.miassolutions.milkledger.utils.milkcalculations.MilkCalculationUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject

class MilkSaleRepository @Inject constructor(
    private val milkDao: MilkDao,
    private val ledgerDao: LedgerDao,
    private val accountDao: AccountDao,
    private val db: AppDatabase
) {


    // Customer ki mukammal history
    fun getCustomerHistory(accountId: String): Flow<List<MilkSaleUiModel>> {
        return milkDao.getCustomerSalesHistory(accountId)
    }

    fun getSalesByDate(start: Long, end: Long): Flow<List<MilkSaleUiModel>> {
        return milkDao.getMilkSalesByDate(start, end)
    }

    fun getCustomers(): Flow<List<Account>> {
        return accountDao.getAccountsByType(AccountType.CUSTOMER).map { list ->
            list.map { it.toDomain() }
        }
    }

    fun getCustomerBalance(accountId: String): Flow<Long> {
        return ledgerDao.getAccountBalance(accountId)
    }

    @Transaction
    suspend fun deleteSale(saleId: String) {
        val currentTime = System.currentTimeMillis()
        milkDao.softDeleteMilkTransaction(saleId, currentTime)
        ledgerDao.softDeleteLedgerByReference(saleId, currentTime)
    }

    suspend fun saveMilkSale(
        saleDate: LocalDate,
        paymentDate: LocalDate,
        accountId: String,
        volume: Double,
        deduction: Double,
        rate: Double,
        amountPaid: Long,
        note: String?
    ) {
        db.withTransaction {
            val netQuantity = volume - deduction

            val totalPriceDouble =
                MilkCalculationUtils.calculateCustomerPrice(volume, deduction, rate)
            val totalPricePaisa = totalPriceDouble.toLongPaisa()

            // 1. Save Milk (Use SALE DATE)
            val milkEntity = MilkTransactionEntity(
                accountId = accountId,
                dateMillis = saleDate.toMillis(),
                type = TransactionType.SALE,
                volume = volume,
                deduction = deduction,
                quantity = netQuantity,
                rateUsed = rate,
                totalAmount = totalPricePaisa,
                notes = note
            )
            milkDao.insert(milkEntity)

            // 2. Save Ledger Debit (Use SALE DATE)
            val saleLedger = FinancialLedgerEntity(
                dateMillis = saleDate.toMillis(),
                accountId = accountId,
                referenceId = milkEntity.milkTransId,
                type = LedgerEntryType.MILK_SALE,
                debit = totalPricePaisa,
                credit = 0,
                profitImpact = totalPricePaisa,
                note = "Milk: $volume - $deduction = $netQuantity Ltr"
            )
            ledgerDao.insert(saleLedger)

            // 3. Save Payment (Use PAYMENT DATE)
            if (amountPaid > 0) {
                val paymentLedger = FinancialLedgerEntity(
                    dateMillis = paymentDate.toMillis(), // ✅ Correct: Uses Payment Date
                    accountId = accountId,
                    type = LedgerEntryType.CASH_RECEIVED,
                    referenceId = milkEntity.milkTransId,
                    debit = 0,
                    credit = amountPaid,
                    profitImpact = 0,
                    note = "Payment with sale"
                )
                ledgerDao.insert(paymentLedger)
            }
        }
    }

    suspend fun getSaleById(id: String): MilkSaleUiModel? {
        return milkDao.getSaleDetailById(id)
    }

    suspend fun updateMilkSale(request: UpdateSaleRequest) {
        db.withTransaction {
            // Calculations
            val netQuantity = request.volume - request.deduction
            val totalPriceDouble = MilkCalculationUtils.calculateCustomerPrice(
                request.volume,
                request.deduction,
                request.rate
            )
            val totalPricePaisa = totalPriceDouble.toLongPaisa()

            val oldSale = milkDao.getMilkTransactionById(request.saleId)
                ?: throw Exception("Sale not found")

            // 1. Update Milk Entity (Use SALE DATE)
            val updatedMilkEntity = oldSale.copy(
                dateMillis = request.date.toMillis(), // ✅ Sale Date
                volume = request.volume,
                deduction = request.deduction,
                quantity = netQuantity,
                rateUsed = request.rate,
                totalAmount = totalPricePaisa,
                notes = request.note,
                updatedAtMillis = System.currentTimeMillis()
            )
            milkDao.update(updatedMilkEntity)

            // 2. Update Ledger Debit (Use SALE DATE)
            val saleLedgerEntry = ledgerDao.getLedgerByReferenceId(request.saleId, LedgerEntryType.MILK_SALE)
            saleLedgerEntry?.let { entry ->
                val updatedLedger = entry.copy(
                    dateMillis = request.date.toMillis(), // ✅ Sale Date
                    debit = totalPricePaisa,
                    profitImpact = totalPricePaisa,
                    note = "Milk: ${request.volume} - ${request.deduction} = $netQuantity Ltr",
                    updatedAtMillis = System.currentTimeMillis()
                )
                ledgerDao.update(updatedLedger)
            }

            // 3. Update Payment (Use PAYMENT DATE) 🔥 Changes Here
            val paymentLedgerEntry = ledgerDao.getLedgerByReferenceId(request.saleId, LedgerEntryType.CASH_RECEIVED)

            if (paymentLedgerEntry != null) {
                if (request.amountPaid > 0) {
                    // Update existing payment
                    val updatedPayment = paymentLedgerEntry.copy(
                        // 🛑 OLD: dateMillis = request.date.toMillis(),
                        // ✅ NEW: Payment Date use karein
                        dateMillis = request.paymentDate.toMillis(),
                        credit = request.amountPaid,
                        updatedAtMillis = System.currentTimeMillis()
                    )
                    ledgerDao.update(updatedPayment)
                } else {
                    ledgerDao.delete(paymentLedgerEntry)
                }
            } else if (request.amountPaid > 0) {
                // Insert new payment (if user added payment during edit)
                val newPaymentLedger = FinancialLedgerEntity(
                    // 🛑 OLD: dateMillis = request.date.toMillis(),
                    // ✅ NEW: Payment Date use karein
                    dateMillis = request.paymentDate.toMillis(),
                    accountId = request.accountId,
                    type = LedgerEntryType.CASH_RECEIVED,
                    referenceId = request.saleId,
                    debit = 0,
                    credit = request.amountPaid,
                    profitImpact = 0,
                    note = "Payment with sale (Updated)"
                )
                ledgerDao.insert(newPaymentLedger)
            }
        }
    }
}
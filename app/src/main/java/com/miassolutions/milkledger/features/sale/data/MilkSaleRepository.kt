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
import com.miassolutions.milkledger.features.purchase.model.SaleSummary
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

    fun getCustomerSummary(accountId: String, start: Long, end: Long): Flow<SaleSummary> {
        return milkDao.getCustomerSummary(accountId, start, end)
    }

    // Customer ki mukammal history
    fun getCustomerHistory(accountId: String, start: Long, end: Long): Flow<List<MilkSaleUiModel>> {
        return milkDao.getCustomerSalesHistory(accountId, start, end)
    }

    fun getSalesByDate(date: Long): Flow<List<MilkSaleUiModel>> {
        return milkDao.getMilkSalesByDate(date)
    }



    fun getCustomers(): Flow<List<Account>> {
        // Sirf Active accounts layen
        return accountDao.getAccountsByType(AccountType.CUSTOMER).map { list ->
            list.filter { it.isActive }.map { it.toDomain() }
        }
    }

    fun getAccountBalance(accountId: String): Flow<Long> {
        return ledgerDao.getAccountBalance(accountId)
    }

    fun getGlobalSaleStats(start: Long, end: Long): Flow<SaleSummary> =
        milkDao.getGlobalSaleStats(start, end)

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
            // ✅ 1. Customer ka naam fetch karein
            val customerName = accountDao.getAccountById(accountId)?.name ?: "Unknown Customer"

            val netQuantity = volume - deduction
            val totalPriceDouble =
                MilkCalculationUtils.calculateCustomerPrice(volume, deduction, rate)
            val totalPricePaisa = totalPriceDouble.toLongPaisa()

            // 2. Save Milk (Use SALE DATE)
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

            // 3. Save Ledger Debit (Use SALE DATE)
            val saleLedger = FinancialLedgerEntity(
                dateMillis = saleDate.toMillis(),
                accountId = accountId,
                referenceId = milkEntity.milkTransId,
                type = LedgerEntryType.MILK_SALE,
                debit = totalPricePaisa,
                credit = 0,
                profitImpact = totalPricePaisa,
                // ✅ Note mein bhi Customer ka naam add kar sakte hain agar chahein
                note = "Milk Sale to $customerName: $volume - $deduction = $netQuantity Ltr"
            )
            ledgerDao.insert(saleLedger)

            // 4. Save Payment (Use PAYMENT DATE)
            if (amountPaid > 0) {
                val paymentLedger = FinancialLedgerEntity(
                    dateMillis = paymentDate.toMillis(),
                    accountId = accountId,
                    type = LedgerEntryType.CASH_RECEIVED,
                    referenceId = milkEntity.milkTransId,
                    debit = 0,
                    credit = amountPaid,
                    profitImpact = 0,
                    // ✅ Yahan specifically "Received from Name" ayega
                    note = "Rec. from $customerName"
                )
                ledgerDao.insert(paymentLedger)
            }
        }
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

            // ✅ Customer ka naam fetch karein (Old sale se accountId le kar)
            val customerName =
                accountDao.getAccountById(oldSale.accountId)?.name ?: "Unknown Customer"

            // 1. Update Milk Entity
            val updatedMilkEntity = oldSale.copy(
                dateMillis = request.date.toMillis(),
                volume = request.volume,
                deduction = request.deduction,
                quantity = netQuantity,
                rateUsed = request.rate,
                totalAmount = totalPricePaisa,
                notes = request.note,
                updatedAtMillis = System.currentTimeMillis()
            )
            milkDao.update(updatedMilkEntity)

            // 2. Update Ledger Debit
            val saleLedgerEntry =
                ledgerDao.getLedgerByReferenceId(request.saleId, LedgerEntryType.MILK_SALE)
            saleLedgerEntry?.let { entry ->
                val updatedLedger = entry.copy(
                    dateMillis = request.date.toMillis(),
                    debit = totalPricePaisa,
                    profitImpact = totalPricePaisa,
                    // ✅ Note Update
                    note = "Milk Sale to $customerName: ${request.volume} - ${request.deduction} = $netQuantity Ltr",
                    updatedAtMillis = System.currentTimeMillis()
                )
                ledgerDao.update(updatedLedger)
            }

            // 3. Update Payment
            val paymentLedgerEntry =
                ledgerDao.getLedgerByReferenceId(request.saleId, LedgerEntryType.CASH_RECEIVED)

            if (paymentLedgerEntry != null) {
                if (request.amountPaid > 0) {
                    val updatedPayment = paymentLedgerEntry.copy(
                        dateMillis = request.paymentDate.toMillis(),
                        credit = request.amountPaid,
                        // ✅ Note Update
                        note = "Received from $customerName",
                        updatedAtMillis = System.currentTimeMillis()
                    )
                    ledgerDao.update(updatedPayment)
                } else {
                    ledgerDao.delete(paymentLedgerEntry)
                }
            } else if (request.amountPaid > 0) {
                // Insert new payment (if user added payment during edit)
                val newPaymentLedger = FinancialLedgerEntity(
                    dateMillis = request.paymentDate.toMillis(),
                    accountId = request.accountId, // Make sure request has accountId, or use oldSale.accountId
                    type = LedgerEntryType.CASH_RECEIVED,
                    referenceId = request.saleId,
                    debit = 0,
                    credit = request.amountPaid,
                    profitImpact = 0,
                    // ✅ Note for new payment
                    note = "Received from $customerName"
                )
                ledgerDao.insert(newPaymentLedger)
            }
        }
    }

    suspend fun getSaleById(id: String): MilkSaleUiModel? {
        return milkDao.getSaleDetailById(id)
    }


}
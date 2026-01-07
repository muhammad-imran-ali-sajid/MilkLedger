package com.miassolutions.milkledger.features.milk

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
import com.miassolutions.milkledger.features.milk.model.UpdateSaleRequest
import com.miassolutions.milkledger.features.sale.domain.model.MilkSaleUiModel
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

        // 1. Milk Table se delete mark karein
        milkDao.softDeleteMilkTransaction(saleId, currentTime)

        // 2. Ledger Table se delete mark karein
        // Note: Chunke Sale aur Payment (agar form me hui thi) dono ki referenceId = saleId hoti hai,
        // to ye aik line dono entries ko delete mark kar degi.
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

            val milkEntity = MilkTransactionEntity(
                accountId = accountId,
                dateMillis = saleDate.toMillis(),
                type = TransactionType.SALE,

                volume =  volume,
                deduction = deduction,
                quantity = netQuantity,
                rateUsed = rate,
                totalAmount = totalPricePaisa,
                notes = note
            )

            milkDao.insert(milkEntity)

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

            if (amountPaid > 0) {
                val paymentLedger = FinancialLedgerEntity(
                    dateMillis = paymentDate.toMillis(),
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


    // Edit Form k liye Data lana
    suspend fun getSaleById(id: String): MilkSaleUiModel? {
        // Direct DAO se bana banaya UI Model milega
        return milkDao.getSaleDetailById(id)
    }
    suspend fun updateMilkSale(request: UpdateSaleRequest) {
        db.withTransaction {
            // 1. Calculations Dobara Karein (Taake data consistent rahy)
            val netQuantity = request.volume - request.deduction

            // Price Calculation (Helper Utils use kr k)
            val totalPriceDouble = MilkCalculationUtils.calculateCustomerPrice(
                request.volume,
                request.deduction,
                request.rate
            )
            val totalPricePaisa = totalPriceDouble.toLongPaisa()

            // 2. Milk Transaction Entity Update Karein
            // Note: Hum Purani entity fetch kr k copy bhi kr skty hen,
            // lekin direct query se update krna zyada fast hai.

            // Lekin Room Update k liye humen Entity object chahiye hota hai.
            // Behtar hai pehle purana fetch kr len taake consistency rahy.
            val oldSale = milkDao.getMilkTransactionById(request.saleId)
                ?: throw Exception("Sale not found") // Safety Check

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

            milkDao.update(updatedMilkEntity) // ✅ Update Milk Table

            // 3. Ledger Update (Sale Entry - Debit)
            // Humen wo Ledger entry dhundni hai jo IS Sale ki hai (MILK_SALE)
            val saleLedgerEntry = ledgerDao.getLedgerByReferenceId(request.saleId, LedgerEntryType.MILK_SALE)

            saleLedgerEntry?.let { entry ->
                val updatedLedger = entry.copy(
                    dateMillis = request.date.toMillis(), // Date bhi update
                    debit = totalPricePaisa,              // Naya Bill Update
                    profitImpact = totalPricePaisa,
                    note = "Milk: ${request.volume} - ${request.deduction} = $netQuantity Ltr",
                    updatedAtMillis = System.currentTimeMillis()
                )
                ledgerDao.update(updatedLedger) // ✅ Update Ledger Debit
            }

            // 4. Payment Update (Thora Complex Part)
            // Check karein k is Sale k sath koi Payment linked thi ya nahi?
            val paymentLedgerEntry = ledgerDao.getLedgerByReferenceId(request.saleId, LedgerEntryType.CASH_RECEIVED)

            if (paymentLedgerEntry != null) {
                if (request.amountPaid > 0) {
                    // Case A: Pehle Payment thi, ab amount change kr dia -> Update
                    val updatedPayment = paymentLedgerEntry.copy(
                        dateMillis = request.date.toMillis(), // Usually Payment date same as Sale date in this flow
                        credit = request.amountPaid,
                        updatedAtMillis = System.currentTimeMillis()
                    )
                    ledgerDao.update(updatedPayment)
                } else {
                    // Case B: Pehle Payment thi, ab user ne 0 kr di -> Delete
                    ledgerDao.delete(paymentLedgerEntry)
                }
            } else if (request.amountPaid > 0) {
                // Case C: Pehle Payment nahi thi, ab user ne daal di -> Insert New
                val newPaymentLedger = FinancialLedgerEntity(
                    dateMillis = request.date.toMillis(),
                    accountId = request.accountId,
                    type = LedgerEntryType.CASH_RECEIVED,
                    referenceId = request.saleId, // Link with Sale ID
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
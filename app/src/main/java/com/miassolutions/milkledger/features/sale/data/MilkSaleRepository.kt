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
import java.time.format.DateTimeFormatter
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

    // Helper to format Date for Note (e.g., "18 Jan")
    private val dateFormatter = DateTimeFormatter.ofPattern("dd MMM")

    // ✅ SAVE SALE FUNCTION
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
            val customerName = accountDao.getAccountById(accountId)?.name ?: "Unknown Customer"

            val netQuantity = volume - deduction
            val totalPriceDouble = MilkCalculationUtils.calculateCustomerPrice(volume, deduction, rate)
            val totalPricePaisa = totalPriceDouble.toLongPaisa()

            // 1. Save Milk (Sale Date par hi rahega - Ye Doodh ka hisaab hy)
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

            // 2. Save Ledger Debit (Sale Date par hi rahega - Ye Bill hy)
            val saleLedger = FinancialLedgerEntity(
                dateMillis = saleDate.toMillis(),
                accountId = accountId,
                referenceId = milkEntity.milkTransId,
                type = LedgerEntryType.MILK_SALE,
                debit = totalPricePaisa,
                credit = 0,
                profitImpact = totalPricePaisa,
                note = "Sale: $customerName: $volume - $deduction = $netQuantity L"
            )
            ledgerDao.insert(saleLedger)

            // 3. Save Payment (Cash Received)
            if (amountPaid > 0) {

                // 🔥 LOGIC: Payment Date sirf Note me dikhana hai
                val isDateDifferent = !saleDate.isEqual(paymentDate)

                val finalNote = buildString {
                    append("$customerName Paid") // Default Note
                    if (isDateDifferent) {
                        // Agar payment date alag hai to note me likh den
                        append(" (${paymentDate.format(dateFormatter)})")
                    }
                }

                val paymentLedger = FinancialLedgerEntity(
                    // 🔥 CRITICAL: Yahan ab hum 'System.currentTimeMillis()' use kar rahe hain
                    // Taake Cashflow Report me ye paisa AAJ (Current Date) me show ho.
                    dateMillis = System.currentTimeMillis(),

                    accountId = accountId,
                    type = LedgerEntryType.CASH_RECEIVED,
                    referenceId = milkEntity.milkTransId,
                    debit = 0,
                    credit = amountPaid,
                    profitImpact = 0,

                    // ✅ Note me date save ho gayi user ki yaad-dehani k liye
                    note = finalNote
                )
                ledgerDao.insert(paymentLedger)
            }
        }
    }

    // ✅ UPDATE SALE FUNCTION
    suspend fun updateMilkSale(request: UpdateSaleRequest) {
        db.withTransaction {
            val netQuantity = request.volume - request.deduction
            val totalPriceDouble = MilkCalculationUtils.calculateCustomerPrice(
                request.volume, request.deduction, request.rate
            )
            val totalPricePaisa = totalPriceDouble.toLongPaisa()

            val oldSale = milkDao.getMilkTransactionById(request.saleId)
                ?: throw Exception("Sale not found")
            val customerName = accountDao.getAccountById(oldSale.accountId)?.name ?: "Unknown Customer"

            // 1. Update Milk Entity (Sale Date)
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

            // 2. Update Ledger Debit (Sale Date)
            val saleLedgerEntry = ledgerDao.getLedgerByReferenceId(request.saleId, LedgerEntryType.MILK_SALE)
            saleLedgerEntry?.let { entry ->
                val updatedLedger = entry.copy(
                    dateMillis = request.date.toMillis(),
                    debit = totalPricePaisa,
                    profitImpact = totalPricePaisa,
                    note = "Sale: $customerName: ${request.volume} - ${request.deduction} = $netQuantity L",
                    updatedAtMillis = System.currentTimeMillis()
                )
                ledgerDao.update(updatedLedger)
            }

            // 3. Update Payment (Cash Flow Logic)
            val paymentLedgerEntry = ledgerDao.getLedgerByReferenceId(request.saleId, LedgerEntryType.CASH_RECEIVED)

            // 🔥 Note Generation Logic
            val userCustomNote = request.note?.trim() // Agar user ne form me koi khaas note likha ho
            val isDateDifferent = !request.date.isEqual(request.paymentDate)

            val finalNote = buildString {
                if (!userCustomNote.isNullOrEmpty()) {
                    append(userCustomNote)
                } else {
                    append("Get from $customerName")
                }

                if (isDateDifferent) {
                    append(" (${request.paymentDate.format(dateFormatter)})")
                }
            }

            if (request.amountPaid > 0) {
                if (paymentLedgerEntry != null) {
                    // --- Update Existing Payment ---
                    val updatedPayment = paymentLedgerEntry.copy(
                        // Agar purani payment edit ho rahi hai, to hum date change NAHI karte
                        // taake purana cashflow disturb na ho.
                        // Lekin agar aap chahty hen k edit krny pr bhi AAJ ki date ho jaye,
                        // to yahan System.currentTimeMillis() laga den.
                        // Filhal hum Existing Date rakh rahy hen aur sirf Amount/Note update kr rahy hen.
                        dateMillis = paymentLedgerEntry.dateMillis,

                        credit = request.amountPaid,
                        note = finalNote,
                        updatedAtMillis = System.currentTimeMillis()
                    )
                    ledgerDao.update(updatedPayment)
                } else {
                    // --- Insert NEW Payment (Recovery) ---
                    // Ye wo case hai jahan pehle payment 0 thi, ab user ne paise add kiye hain.
                    // Yahan hum Lazmi AAJ KI DATE lagayenge.
                    val newPaymentLedger = FinancialLedgerEntity(

                        // 🔥 CRITICAL: New Payment = Aaj ka Cashflow
                        dateMillis = System.currentTimeMillis(),

                        accountId = request.accountId,
                        type = LedgerEntryType.CASH_RECEIVED,
                        referenceId = request.saleId,
                        debit = 0,
                        credit = request.amountPaid,
                        profitImpact = 0,
                        note = finalNote
                    )
                    ledgerDao.insert(newPaymentLedger)
                }
            } else {
                // Delete Payment
                if (paymentLedgerEntry != null) {
                    ledgerDao.delete(paymentLedgerEntry)
                }
            }
        }
    }

    suspend fun getSaleById(id: String): MilkSaleUiModel? {
        return milkDao.getSaleDetailById(id)
    }


}
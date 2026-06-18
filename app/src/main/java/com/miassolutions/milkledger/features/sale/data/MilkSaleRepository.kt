package com.miassolutions.milkledger.features.sale.data

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
import com.miassolutions.milkledger.features.sale.model.UpdateSaleRequest
import com.miassolutions.milkledger.utils.extensions.toMillis
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject

class MilkSaleRepository @Inject constructor(
    private val milkDao: MilkDao,
    private val ledgerDao: LedgerDao,
    private val accountDao: AccountDao,
    private val backupRepository: BackupRepository,
    private val db: AppDatabase,
    private val saleLedgerFactory: SaleLedgerFactory = SaleLedgerFactory()
) {

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

    suspend fun saveMilkSale(
        saleDate: LocalDate,
        paymentDate: LocalDate?,
        accountId: String,
        volume: Double,
        deduction: Double,
        rate: Double,
        amountPaid: Long,
        note: String?
    ) {
        db.withTransaction {
            val customerName = accountDao.getAccountById(accountId)?.name ?: "Unknown Customer"
            val amounts = saleLedgerFactory.calculateAmounts(volume, deduction, rate)

            val milkEntity = saleLedgerFactory.createMilkTransaction(
                saleDate = saleDate,
                paymentDate = paymentDate,
                accountId = accountId,
                volume = volume,
                deduction = deduction,
                rate = rate,
                totalPricePaisa = amounts.totalPricePaisa,
                netQuantity = amounts.netQuantity,
                note = note
            )
            milkDao.insert(milkEntity)

            ledgerDao.insert(
                saleLedgerFactory.createSaleLedger(
                    saleDate = saleDate,
                    accountId = accountId,
                    saleId = milkEntity.milkTransId,
                    volume = volume,
                    deduction = deduction,
                    netQuantity = amounts.netQuantity,
                    totalPricePaisa = amounts.totalPricePaisa
                )
            )

            if (amountPaid > 0) {
                val paymentNote = saleLedgerFactory.paymentNote(customerName, saleDate, paymentDate)

                ledgerDao.insert(
                    saleLedgerFactory.createCashReceivedLedger(
                        saleDate = saleDate,
                        accountId = accountId,
                        saleId = milkEntity.milkTransId,
                        amountPaid = amountPaid,
                        note = paymentNote
                    )
                )
            }
        }
        backupRepository.markDataChanged()
    }

    suspend fun updateMilkSale(request: UpdateSaleRequest) {
        db.withTransaction {
            val amounts = saleLedgerFactory.calculateAmounts(
                request.volume,
                request.deduction,
                request.rate
            )

            val oldSale =
                milkDao.getMilkTransactionById(request.saleId) ?: throw Exception("Sale not found")
            val customerName =
                accountDao.getAccountById(oldSale.accountId)?.name ?: "Unknown Customer"

            val updatedMilkEntity = oldSale.copy(
                dateMillis = request.date.toMillis(),
                paymentDateMillis = request.paymentDate?.toMillis(),
                volume = request.volume,
                deduction = request.deduction,
                quantity = amounts.netQuantity,
                rateUsed = request.rate,
                totalAmount = amounts.totalPricePaisa,
                notes = request.note,
                updatedAtMillis = System.currentTimeMillis()
            )
            milkDao.update(updatedMilkEntity)

            val saleLedgerEntry =
                ledgerDao.getLedgerByReferenceId(request.saleId, LedgerEntryType.MILK_SALE)
            saleLedgerEntry?.let { entry ->
                ledgerDao.update(
                    entry.copy(
                        dateMillis = request.date.toMillis(),
                        debit = amounts.totalPricePaisa,
                        profitImpact = amounts.totalPricePaisa,
                        note = saleLedgerFactory.saleNote(
                            request.volume,
                            request.deduction,
                            amounts.netQuantity
                        ),
                        updatedAtMillis = System.currentTimeMillis()
                    )
                )
            }

            val paymentLedgerEntry =
                ledgerDao.getLedgerByReferenceId(request.saleId, LedgerEntryType.CASH_RECEIVED)

            if (request.amountPaid > 0) {
                val paymentNote =
                    saleLedgerFactory.paymentNote(customerName, request.date, request.paymentDate)

                if (paymentLedgerEntry != null) {
                    ledgerDao.update(
                        paymentLedgerEntry.copy(
                            dateMillis = request.date.toMillis(),
                            credit = request.amountPaid,
                            note = paymentNote,
                            updatedAtMillis = System.currentTimeMillis()
                        )
                    )
                } else {
                    ledgerDao.insert(
                        saleLedgerFactory.createCashReceivedLedger(
                            saleDate = request.date,
                            accountId = request.accountId,
                            saleId = request.saleId,
                            amountPaid = request.amountPaid,
                            note = paymentNote
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

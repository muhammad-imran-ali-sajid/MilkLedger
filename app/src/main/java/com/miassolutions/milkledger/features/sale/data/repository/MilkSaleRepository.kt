package com.miassolutions.milkledger.features.sale.data.repository

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
import com.miassolutions.milkledger.features.customer.domain.Customer
import com.miassolutions.milkledger.features.sale.domain.model.MilkSaleUiModel
import com.miassolutions.milkledger.utils.extensions.toLongPaisa
import com.miassolutions.milkledger.utils.extensions.toPrice
import com.miassolutions.milkledger.utils.milkcalculations.MilkCalculationUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
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

    suspend fun saveMilkSale(
        dateMillis: Long,
        accountId: String,
        volume: Double,
        deduction: Double,
        rate: Double,
        amountPaid: Long, //paisa
        note: String?
    ) {
        db.withTransaction {
            val netQuantity = volume - deduction

            val totalPriceDouble =
                MilkCalculationUtils.calculateCustomerPrice(volume, deduction, rate)
            val totalPricePaisa = totalPriceDouble.toLongPaisa()

            val milkEntity = MilkTransactionEntity(
                accountId = accountId,
                dateMillis = dateMillis,
                type = TransactionType.SALE,
                quantity = netQuantity,
                rateUsed = rate,
                totalAmount = totalPricePaisa,
                notes = note
            )

            milkDao.insert(milkEntity)

            val saleLedger = FinancialLedgerEntity(
                dateMillis = dateMillis,
                accountId = accountId,
                referenceId = milkEntity.milkTransId,
                type = LedgerEntryType.MILK_SALE,
                debit = totalPricePaisa,
                credit = 0,
                profitImpact = totalPricePaisa,
                note = "Milk sale: $netQuantity liters"
            )

            ledgerDao.insert(saleLedger)

            if (amountPaid > 0) {
                val paymentLedger = FinancialLedgerEntity(
                    dateMillis = dateMillis,
                    accountId = accountId,
                    referenceId = null,
                    type = LedgerEntryType.CASH_RECEIVED,
                    debit = 0,
                    credit = amountPaid,
                    profitImpact = 0,
                    note = "Payment with sale"
                )

                ledgerDao.insert(paymentLedger)
            }

        }

    }

}

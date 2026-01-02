package com.miassolutions.milkledger.features.transaction.data


import com.miassolutions.milkledger.features.sale.domain.model.BalanceHistoryItem
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject

class TransactionRepository @Inject constructor(
    private val transactionDao: TransactionDao
) {

    suspend fun getCustomerBalanceHistory(customerId: String): List<BalanceHistoryItem> {
        val ledger = transactionDao.customerLedger(customerId)

        var runningBalance = 0.0

        return ledger.map { row ->

            val delta = row.credit - row.debit
            runningBalance += delta

            BalanceHistoryItem(
                date = Instant
                    .ofEpochMilli(row.dateMillis)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate(),
                change = delta,
                balanceAfter = runningBalance,
                note = row.note)
        }
    }

}
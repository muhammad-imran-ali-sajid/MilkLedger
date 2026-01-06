package com.miassolutions.milkledger.features.milk.balancehistory

import androidx.lifecycle.ViewModel
import com.miassolutions.milkledger.core.localdb.ledger.LedgerDao
import com.miassolutions.milkledger.features.customer.domain.model.BalanceHistoryUi
import com.miassolutions.milkledger.features.customer.domain.model.CustomerHistoryUi
import com.miassolutions.milkledger.features.customer.domain.model.HistoryType
import com.miassolutions.milkledger.utils.extensions.toLocalDate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class CustomerHistoryViewModel @Inject constructor(
    private val ledgerDao: LedgerDao // Repository use karna behtar hai, but direct DAO for brevity
) : ViewModel() {

    fun getRunningBalanceHistory(accountId: String): Flow<List<BalanceHistoryUi>> {
        return ledgerDao.getLedgerForRunningBalance(accountId).map { rawList ->

            var currentRunningBalance = 0L
            val uiList = mutableListOf<BalanceHistoryUi>()

            // 1. Loop through list (Oldest to Newest)
            rawList.forEach { entity ->

                // Logic: Debit (+) barhata hai, Credit (-) kam karta hai
                // (Kyunke hum Customer k point of view se dekh rahy hen: Usne Dena hai)
                val transactionAmount = if (entity.debit > 0) entity.debit else entity.credit
                val isDebit = entity.debit > 0

                if (isDebit) {
                    currentRunningBalance += entity.debit  // Udhaar barh gaya
                } else {
                    currentRunningBalance -= entity.credit // Paise aa gaye, Udhaar kam hua
                }

                uiList.add(
                    BalanceHistoryUi(
                        id = entity.ledgerId,
                        date = entity.dateMillis.toLocalDate(),
                        description = entity.note ?: entity.type.name,
                        amount = transactionAmount,
                        isDebit = isDebit,
                        runningBalance = currentRunningBalance // ✅ Calculated Balance
                    )
                )
            }

            // 2. Result ko Reverse karein (Newest on Top) taake user ko aaj ka balance upar dikhe
            uiList.reversed()
        }
    }
}
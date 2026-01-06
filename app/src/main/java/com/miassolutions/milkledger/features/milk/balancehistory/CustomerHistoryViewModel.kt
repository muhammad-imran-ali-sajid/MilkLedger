package com.miassolutions.milkledger.features.milk.balancehistory

import androidx.lifecycle.ViewModel
import com.miassolutions.milkledger.core.localdb.ledger.LedgerDao
import com.miassolutions.milkledger.features.milk.model.BalanceHistoryUi
import com.miassolutions.milkledger.features.milk.model.DailyBalanceUi
import com.miassolutions.milkledger.utils.extensions.toLocalDate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
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

//                uiList.add(
//                    BalanceHistoryUi(
//                        id = entity.ledgerId,
//                        date = entity.dateMillis.toLocalDate(),
//                        description = entity.note ?: entity.type.name,
//                        amount = transactionAmount,
//                        isDebit = isDebit,
//                        runningBalance = currentRunningBalance // ✅ Calculated Balance
//                    )
//                )
            }

            // 2. Result ko Reverse karein (Newest on Top) taake user ko aaj ka balance upar dikhe
            uiList.reversed()
        }
    }

    fun getDailyClosingBalance(accountId: String): Flow<List<BalanceHistoryUi>> {
        // 1. Raw Data mangwayen (Shuru se akhir tak - ASC)
        return ledgerDao.getLedgerForRunningBalance(accountId).map { rawList ->

            val dailyMap = mutableMapOf<LocalDate, BalanceHistoryUi>()
            var runningBalance = 0L

            // 2. Loop through every transaction
            rawList.forEach { entity ->

                // Calculate Transaction Amount (+ for Debit, - for Credit)
                val transactionAmount = if (entity.debit > 0) entity.debit else -entity.credit

                // Update Global Running Balance
                runningBalance += transactionAmount

                val date = entity.dateMillis.toLocalDate()

                // 3. Map Update Logic:
                // Chunke hum ASC order me chal rahy hen, to is date ka jo "Aakhri" runningBalance hoga
                // wo automatially map me save ho jayega (Overwrite hota rahega).

                // Humen us din ka Net Change bhi calculate krna hai:
                val existingNetChange = dailyMap[date]?.netChange ?: 0L

                dailyMap[date] = BalanceHistoryUi(
                    date = date,
                    closingBalance = runningBalance, // ✅ Ye us moment tak ka balance hai
                    netChange = existingNetChange + transactionAmount // Us din ka kul hisaab
                )
            }

            // 4. Map ko List bana kar Reverse karein (Newest Date on Top)
            dailyMap.values.toList().sortedByDescending { it.date }
        }
    }
}
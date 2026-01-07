package com.miassolutions.milkledger.features.milk.balancehistory

import androidx.lifecycle.ViewModel
import com.miassolutions.milkledger.core.localdb.ledger.LedgerDao
import com.miassolutions.milkledger.features.milk.model.BalanceHistoryUi
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
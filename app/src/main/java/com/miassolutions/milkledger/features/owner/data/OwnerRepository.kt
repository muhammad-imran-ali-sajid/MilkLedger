package com.miassolutions.milkledger.features.owner.data


import androidx.room.withTransaction
import com.miassolutions.milkledger.core.contstants.Constants.OWNER_ACCOUNT_ID
import com.miassolutions.milkledger.core.localdb.AppDatabase
import com.miassolutions.milkledger.core.localdb.ledger.FinancialLedgerEntity
import com.miassolutions.milkledger.core.localdb.ledger.LedgerDao
import com.miassolutions.milkledger.core.localdb.ledger.LedgerEntryType
import com.miassolutions.milkledger.features.owner.domain.OwnerDashboardData
import com.miassolutions.milkledger.features.owner.domain.OwnerTransactionUiModel
import com.miassolutions.milkledger.utils.extensions.toMillis
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

class OwnerRepository @Inject constructor(
    private val ledgerDao: LedgerDao,
    private val db: AppDatabase
) {

    // ------------------------------------------------
    // 1️⃣ DASHBOARD DATA (Combined Flow)
    // ------------------------------------------------
    // Hum Profit, Drawing, aur List ko aik hi Flow me combine kar k denge
    // Taake ViewModel me 3 alag alag collectors na lagane paren.

    fun getDashboardData(start: Long, end: Long): Flow<OwnerDashboardData> {
        return combine(
            ledgerDao.getNetProfitInRange(start, end),
            ledgerDao.getTotalDrawingsInRange(start, end),
            ledgerDao.getOwnerTransactionsInRange(start, end)
        ) { profit, drawings, transactions ->

            // Map Entity to UI Model
            val uiTransactions = transactions.map { entity ->

                // 1. Title Logic:
                // Agar Note khali hai tu "Cash Withdrawal" (Fallback)
                // Agar Personal Expense tha, tu wahan se Title 'note' me save hoa tha, wo yahan show ho jayega.
                val displayTitle = entity.note ?: "Cash Withdrawal"

                // 2. Icon Logic (Distinction):
                // Kese pata chalay k ye Cash hai ya Expense?
                // Expense Repository me humne note save kia tha: "${item.title}"
                // Cash Withdrawal me humne default rakha tha "Cash Withdrawal"

                // Simple Check: Agar title "Cash Withdrawal" nahi hai, tu ye Personal Expense hai
                // (Ya phir aap specific prefix use kr skty hen future me)
                val isExpense = !displayTitle.equals("Cash Withdrawal", ignoreCase = true)

                OwnerTransactionUiModel(
                    id = entity.ledgerId,
                    dateMillis = entity.dateMillis,
                    amount = entity.debit,

                    // 🔥 CHANGE: Ab hum 'note' ko as a Title bhej rahe hain
                    // Agar Personal Expense hoga to uska Title (e.g. "Grocery") show hoga.
                    note = displayTitle,

                    isPersonalExpense = isExpense
                )
            }

            OwnerDashboardData(
                netProfit = profit,
                totalDrawings = drawings,
                transactions = uiTransactions
            )
        }
    }

    // ------------------------------------------------
    // 2️⃣ SAVE CASH WITHDRAWAL
    // ------------------------------------------------
    // Jab owner "Withdraw" button dabaye ga
    suspend fun saveCashWithdrawal(amount: Long, date: LocalDate, note: String?) {
        db.withTransaction {
            val entry = FinancialLedgerEntity(
                dateMillis = date.toMillis(),
                accountId = OWNER_ACCOUNT_ID,
                type = LedgerEntryType.OWNER_DRAWING,
                referenceId = UUID.randomUUID().toString(), // No external reference

                debit = amount, // Paisa nikala (Debit)
                credit = 0,

                profitImpact = 0, // No impact on profit
                note = note ?: "Cash Withdrawal"
            )
            ledgerDao.insert(entry)
        }
    }

    suspend fun updateCashWithdrawal(ledgerId: String, amount: Long, date: LocalDate, note: String?) {
        db.withTransaction {
            val oldEntry = ledgerDao.getLedgerById(ledgerId) // DAO me getById hona chahiye

            oldEntry?.let { entry ->
                val updated = entry.copy(
                    dateMillis = date.toMillis(),
                    debit = amount, // Update Amount
                    note = note ?: "Cash Withdrawal",
                    updatedAtMillis = System.currentTimeMillis(),
                    isSynced = false
                )
                ledgerDao.update(updated)
            }
        }
    }


    suspend fun deleteTransaction(ledgerId: String) {
        db.withTransaction {
            // Sirf Ledger table se delete hoga (Soft Delete)
            ledgerDao.softDeleteLedgerById(ledgerId, System.currentTimeMillis())
        }
    }



}
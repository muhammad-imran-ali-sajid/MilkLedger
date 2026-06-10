package com.miassolutions.milkledger.features.owner.data


import androidx.room.withTransaction
import com.miassolutions.milkledger.core.contstants.Constants
import com.miassolutions.milkledger.core.contstants.Constants.OWNER_ACCOUNT_ID
import com.miassolutions.milkledger.core.localdb.database.AppDatabase
import com.miassolutions.milkledger.core.localdb.account.local.AccountDao
import com.miassolutions.milkledger.core.localdb.account.local.AccountEntity
import com.miassolutions.milkledger.core.localdb.ledger.FinancialLedgerEntity
import com.miassolutions.milkledger.core.localdb.ledger.LedgerDao
import com.miassolutions.milkledger.core.localdb.ledger.LedgerEntryType
import com.miassolutions.milkledger.features.owner.domain.DailyProfitTuple
import com.miassolutions.milkledger.features.owner.domain.OwnerDashboardData
import com.miassolutions.milkledger.features.owner.domain.OwnerTransactionUiModel
import com.miassolutions.milkledger.features.owner.domain.ProfitUiModel
import com.miassolutions.milkledger.utils.extensions.toMillis
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

class OwnerRepository @Inject constructor(
    private val ledgerDao: LedgerDao,
    private val accountDao: AccountDao,
    private val db: AppDatabase
) {

    suspend fun getOwner(): AccountEntity? = accountDao.getOwner()

    suspend fun saveOwner(owner: AccountEntity) {
        accountDao.upsert(owner)
    }

    suspend fun getProfitReportList(start: Long, end: Long): List<ProfitUiModel> {
        return ledgerDao.getProfitReportList(start, end)
    }


    fun getProfitBreakdown(start: Long, end: Long): Flow<List<DailyProfitTuple>> {
        return ledgerDao.getDailyProfitBreakdown(start, end)
    }

    // ------------------------------------------------
    // 1️⃣ DASHBOARD DATA (Combined Flow)
    // ------------------------------------------------
    // Hum Profit, Drawing, aur List ko aik hi Flow me combine kar k denge
    // Taake ViewModel me 3 alag alag collectors na lagane paren.

    // ------------------------------------------------
    // 1️⃣ DASHBOARD DATA (Combined Flow)
    // ------------------------------------------------
    fun getDashboardData(start: Long, end: Long): Flow<OwnerDashboardData> {
        return combine(
            ledgerDao.getNetProfitInRange(start, end),
            ledgerDao.getTotalDrawingsInRange(start, end),

            // 🔥 NAYA: Retained Profit hamesha 'end' date tak nikalein
            ledgerDao.getRetainedProfitUntil(end),

            ledgerDao.getOwnerTransactionsInRange(start, end)
        ) { profit, drawings, retained, transactions ->

            val uiTransactions = transactions.map { entity ->
                val isExpense = entity.referenceId?.startsWith(Constants.PREFIX_EXPENSE)
                val displayTitle = entity.note ?: "Cash Withdrawal"

                OwnerTransactionUiModel(
                    id = entity.ledgerId,
                    dateMillis = entity.dateMillis,
                    amount = entity.debit,
                    note = displayTitle,
                    isPersonalExpense = isExpense == true
                )
            }

            OwnerDashboardData(
                netProfit = profit,
                totalDrawings = drawings,

                // ✅ NAYA: State mein bhej diya
                retainedEarnings = retained,

                transactions = uiTransactions
            )
        }
    }

    fun getRetainedProfitUntil(endDate: Long): Flow<Long> {
        return ledgerDao.getRetainedProfitUntil(endDate)
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

    suspend fun updateCashWithdrawal(
        ledgerId: String,
        amount: Long,
        date: LocalDate,
        note: String?
    ) {
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
package com.miassolutions.milkledger.features.account.data

import androidx.room.withTransaction
import com.miassolutions.milkledger.core.localdb.AppDatabase
import com.miassolutions.milkledger.core.localdb.account.local.AccountDao
import com.miassolutions.milkledger.core.localdb.account.local.AccountEntity
import com.miassolutions.milkledger.core.localdb.account.local.AccountType
import com.miassolutions.milkledger.core.localdb.account.local.toDomain
import com.miassolutions.milkledger.core.localdb.account.local.toEntity
import com.miassolutions.milkledger.core.localdb.ledger.FinancialLedgerEntity
import com.miassolutions.milkledger.core.localdb.ledger.LedgerDao
import com.miassolutions.milkledger.core.localdb.ledger.LedgerEntryType
import com.miassolutions.milkledger.features.account.domain.Account
import com.miassolutions.milkledger.utils.extensions.toLocalDate
import com.miassolutions.milkledger.utils.extensions.toMillis
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject

class AccountRepository @Inject constructor(
    private val dao: AccountDao,
    private val ledgerDao: LedgerDao,
    private val db: AppDatabase
) {

    suspend fun getCurrentBalance(accountId: String): Long {
        return ledgerDao.getAccountNetBalance(accountId) ?: 0L
    }

    suspend fun getOwner(): AccountEntity? = dao.getOwner()

    suspend fun saveOwner(owner: AccountEntity) {
        dao.upsert(owner)
    }

    // 1. Get List
    // Repository
    fun getAccountsByType(type: AccountType): Flow<List<Account>> {
        // Sirf Active accounts layen
        return dao.getAccountsByType(type).map { list ->
            list.filter { it.isActive } .map { it.toDomain() }
        }
    }

    suspend fun isSortOrderExist(sortOrder: Int, accountType: AccountType, excludeId: String? = null): Boolean =
        dao.isSortOrderExist(sortOrder, accountType, excludeId)

    // 2. Get Single Account
    suspend fun getAccountById(id: String): Account? {
        return dao.getAccountById(id)?.toDomain()
    }


    suspend fun getOpeningDate(accountId: String): LocalDate? {
        val entry = ledgerDao.getOpeningBalanceEntry(accountId)
        return entry?.dateMillis?.toLocalDate()
    }

    // ---------------------------------------------------------
    // 3. Save (Insert / Update) with Date Logic 📅
    // ---------------------------------------------------------
    suspend fun saveAccount(account: Account, openingDate: LocalDate) {

        val entity = account.toEntity()

        db.withTransaction {
            // Step A: Account Table me Save/Update
            // (Hum initialBalance save kr rhe hen taake UI me dikha saken,
            // lekin calculations Ledger table se hongi)
            dao.insert(entity)

            // Step B: Ledger Logic (Opening Balance)
            // 1. Check karein agar pehle se entry mojood hai (Edit Case)
            val existingEntry = ledgerDao.getOpeningBalanceEntry(entity.accountId) // DAO me ye query honi chahiye

            val balance = entity.initialBalance ?: 0L

            if (balance != 0L) {
                // Logic: Kis side par likhna hai?
                // Customer: (+ means Debit/Udhaar), (- means Credit/Advance)
                // Supplier: (+ means Credit/Udhaar), (- means Debit/Advance)

                val isCustomer = entity.accountType == AccountType.CUSTOMER

                // Debit Calculation
                val debitAmount = if (isCustomer) {
                    if (balance > 0) balance else 0
                } else {
                    if (balance < 0) -balance else 0 // Supplier ko advance dia (Negative input)
                }

                // Credit Calculation
                val creditAmount = if (isCustomer) {
                    if (balance < 0) -balance else 0 // Customer ne advance dia (Negative input)
                } else {
                    if (balance > 0) balance else 0
                }

                if (existingEntry != null) {
                    // --- UPDATE EXISTING ---
                    val updatedEntry = existingEntry.copy(
                        dateMillis = openingDate.toMillis(), // 🔥 Update Date
                        debit = debitAmount,
                        credit = creditAmount,
                        updatedAtMillis = System.currentTimeMillis()
                    )
                    ledgerDao.update(updatedEntry)
                } else {
                    // --- INSERT NEW ---
                    val newEntry = FinancialLedgerEntity(
                        accountId = entity.accountId,
                        dateMillis = openingDate.toMillis(), // 🔥 Use Selected Date
                        referenceId = entity.accountId, // Self Reference
                        type = LedgerEntryType.OPENING_BALANCE,

                        debit = debitAmount,
                        credit = creditAmount,

                        profitImpact = 0,
                        note = "Opening Balance"
                    )
                    ledgerDao.insert(newEntry)
                }
            } else {
                // Agar Balance 0 kar dia user ne edit kr k, to purani entry delete kr den
                if (existingEntry != null) {
                    ledgerDao.deleteOpeningBalance(entity.accountId)
                }
            }
        }
    }

    // 4. Soft Delete
    suspend fun deleteAccount(accountId: String) {
        db.withTransaction {
            val currentTime = System.currentTimeMillis()
            dao.softDelete(accountId, currentTime)

            // Soft delete the opening balance transaction too
            // (Agar aap hard delete krna chahen to ledgerDao.deleteOpeningBalance use karen)
            // Lekin behtar hai Ledger me bhi soft delete ho.
             ledgerDao.softDeleteOpeningBalance(accountId, currentTime)

            // Filhal aapki logic k mutabiq hard delete:
//            ledgerDao.deleteOpeningBalance(accountId)
        }
    }

    suspend fun restoreAccount(accountId: String) {
        dao.restore(accountId)
    }

    suspend fun permanentlyDeleteAllSoftDeletedAccounts() {
        dao.permanentlyDeleteAllAccounts()
    }
}
package com.miassolutions.milkledger.features.expense.data.repository

import androidx.room.withTransaction
import com.miassolutions.milkledger.core.contstants.Constants
import com.miassolutions.milkledger.core.contstants.Constants.OWNER_ACCOUNT_ID
import com.miassolutions.milkledger.core.contstants.Constants.SHOP_EXPENSE
import com.miassolutions.milkledger.core.localdb.database.AppDatabase
import com.miassolutions.milkledger.core.localdb.account.local.AccountDao
import com.miassolutions.milkledger.core.localdb.account.local.AccountEntity
import com.miassolutions.milkledger.core.localdb.account.local.AccountType
import com.miassolutions.milkledger.core.localdb.expense.ExpenseDao
import com.miassolutions.milkledger.core.localdb.ledger.FinancialLedgerEntity
import com.miassolutions.milkledger.core.localdb.ledger.LedgerDao
import com.miassolutions.milkledger.core.localdb.ledger.LedgerEntryType
import com.miassolutions.milkledger.features.backup.data.BackupRepository
import com.miassolutions.milkledger.features.expense.data.mapper.toDomain
import com.miassolutions.milkledger.features.expense.data.mapper.toEntity
import com.miassolutions.milkledger.features.expense.domain.Expense
import com.miassolutions.milkledger.utils.extensions.toMillis
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ExpenseRepository @Inject constructor(
    private val expenseDao: ExpenseDao,
    private val ledgerDao: LedgerDao,
    private val accountDao: AccountDao,
    private val backupRepository: BackupRepository,
    private val db: AppDatabase
) {


    // 🔥 HELPER: Ensure System Accounts Exist
    private suspend fun ensureSystemAccountsExist() {
        // 1. Check Shop Expense Account (Business Expenses k liye zaroori)
        if (accountDao.isAccountExist(Constants.SHOP_EXPENSE) == 0) {
            accountDao.insertIgnore(
                AccountEntity(
                    accountId = SHOP_EXPENSE,
                    name = "Shop Expense", // Ye user ko nahi dikhega, internal hai
                    accountType = AccountType.OWNER,
                    phone = null,
                    initialBalance = 0L,
                    createdAtMillis = System.currentTimeMillis(),
                    advanceAmount = 0L
                )
            )
        }

        // 2. Check Owner Account (Personal Withdrawals k liye zaroori)
        if (accountDao.isAccountExist(Constants.OWNER_ACCOUNT_ID) == 0) {
            accountDao.insertIgnore(
                AccountEntity(
                    accountId = Constants.OWNER_ACCOUNT_ID,
                    name = "Owner",
                    accountType = AccountType.OWNER,
                    phone = null,
                    initialBalance = 0L,
                    createdAtMillis = System.currentTimeMillis(),
                    advanceAmount = 0L
                )
            )
        }
    }

    // ------------------------------------------------
    // 1️⃣ SAVE ALL (Batch Insert)
    // ------------------------------------------------
    suspend fun saveAllExpenses(expenses: List<Expense>) {
        db.withTransaction {

            ensureSystemAccountsExist()

            val entities = expenses.map { it.toEntity() }
            expenseDao.insertAll(entities)

            val ledgerEntries = entities.map { expenseEntity ->
                // Logic Selection
                val isPersonal = expenseEntity.isPersonal
                val accountId = if (isPersonal) OWNER_ACCOUNT_ID else SHOP_EXPENSE
                val type = if (isPersonal) LedgerEntryType.OWNER_DRAWING else LedgerEntryType.BUSINESS_EXPENSE

                // Profit Impact Logic
                val profitImpact = if (isPersonal) 0L else -(expenseEntity.amount)

                // 🔥 FIX: Note Logic Behtar ki hai
                // Agar Note hai to "Title - Note" banayen, warna sirf "Title"
                val displayNote = if (!expenseEntity.note.isNullOrBlank()) {
                    "${expenseEntity.title} - ${expenseEntity.note}"
                } else {
                    expenseEntity.title
                }

                FinancialLedgerEntity(
                    dateMillis = expenseEntity.dateMillis,
                    accountId = accountId,
                    referenceId = expenseEntity.expenseId,
                    type = type,

                    debit = expenseEntity.amount,
                    credit = 0,

                    profitImpact = profitImpact,
                    note = displayNote // ✅ Ab yahan details ayengi
                )
            }
            ledgerDao.insertAll(ledgerEntries)
        }
        backupRepository.markDataChanged()
    }

    // ------------------------------------------------
    // 2️⃣ SAVE SINGLE (Single Insert)
    // ------------------------------------------------
    suspend fun saveExpense(expense: Expense) {
        db.withTransaction {

            ensureSystemAccountsExist()
            // 1. Expense Table
            val expenseEntity = expense.toEntity()
            expenseDao.insertExpense(expenseEntity)

            // Logic Selection
            val isPersonal = expense.isPersonal
            val accountId = if (isPersonal) OWNER_ACCOUNT_ID else SHOP_EXPENSE
            val type = if (isPersonal) LedgerEntryType.OWNER_DRAWING else LedgerEntryType.BUSINESS_EXPENSE
            val profitImpact = if (isPersonal) 0L else -(expenseEntity.amount)

            // 🔥 FIX: Note Logic Here
            val displayNote = if (!expense.note.isNullOrBlank()) {
                "${expense.title} - ${expense.note}"
            } else {
                expense.title
            }

            // 2. Ledger Table
            val ledgerEntry = FinancialLedgerEntity(
                dateMillis = expense.date.toMillis(),
                accountId = accountId,
                referenceId = expenseEntity.expenseId,
                type = type,

                debit = expenseEntity.amount,
                credit = 0,

                profitImpact = profitImpact,
                note = displayNote // ✅ Fixed: Pehle yahan sirf title tha
            )
            ledgerDao.insert(ledgerEntry)
        }
        backupRepository.markDataChanged()
    }

    // ------------------------------------------------
    // 3️⃣ UPDATE (Critical Logic)
    // ------------------------------------------------
    suspend fun updateExpense(updatedExpense: Expense) {
        db.withTransaction {
            // 1. Expense Table Update
            expenseDao.updateExpense(updatedExpense.toEntity())

            // 2. Ledger Update
            val oldLedgerEntry = ledgerDao.getByReferenceId(updatedExpense.expenseId)

            oldLedgerEntry?.let { entry ->

                // Logic Re-Check
                val isPersonal = updatedExpense.isPersonal
                val newAccountId = if (isPersonal) OWNER_ACCOUNT_ID else SHOP_EXPENSE
                val newType = if (isPersonal) LedgerEntryType.OWNER_DRAWING else LedgerEntryType.BUSINESS_EXPENSE
                val newProfitImpact = if (isPersonal) 0L else -(updatedExpense.amount)

                // 🔥 FIX: Note Logic Here as well
                val displayNote = if (!updatedExpense.note.isNullOrBlank()) {
                    "${updatedExpense.title} - ${updatedExpense.note}"
                } else {
                    updatedExpense.title
                }

                val newLedgerEntry = entry.copy(
                    dateMillis = updatedExpense.date.toMillis(),

                    accountId = newAccountId,
                    type = newType,

                    debit = updatedExpense.amount,
                    credit = 0,

                    profitImpact = newProfitImpact,
                    note = displayNote, // ✅ Fixed: Pehle yahan sirf title tha

                    isSynced = false,
                    updatedAtMillis = System.currentTimeMillis()
                )

                ledgerDao.update(newLedgerEntry)
            }
        }
        backupRepository.markDataChanged()
    }

    // ... (Baki functions same rahenge Delete aur Read walay) ...
    // ------------------------------------------------
    // 4️⃣ DELETE
    // ------------------------------------------------
    suspend fun deleteExpense(expenseId: String) {
        db.withTransaction {
            val currentTime = System.currentTimeMillis()
            expenseDao.softDeleteExpense(expenseId, currentTime)
            ledgerDao.softDeleteByReference(expenseId, currentTime)
        }
        backupRepository.markDataChanged()
    }

    // ------------------------------------------------
    // 5️⃣ READ Operations
    // ------------------------------------------------

    fun getExpensesByDateRange(start: Long, end: Long): Flow<List<Expense>> {
        return expenseDao.getExpensesByDateRange(start, end).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    fun getBusinessExpenses(): Flow<List<Expense>> {
        return expenseDao.getAllBusinessExpenses().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    fun getPersonalExpenses(): Flow<List<Expense>> {
        return expenseDao.getAllPersonalExpenses().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun getExpenseById(id: String): Expense? {
        return expenseDao.getExpenseById(id)?.toDomain()
    }

    fun getTotalExpenseAmount(start: Long, end: Long): Flow<Long> {
        return expenseDao.getTotalExpenseAmount(start, end).map { total ->
            total?.toLong() ?: 0L
        }
    }
}
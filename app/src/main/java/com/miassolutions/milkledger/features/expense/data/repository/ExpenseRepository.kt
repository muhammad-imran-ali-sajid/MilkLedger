package com.miassolutions.milkledger.features.expense.data.repository

import androidx.room.withTransaction
import com.miassolutions.milkledger.core.contstants.Constants.OWNER_ACCOUNT_ID
import com.miassolutions.milkledger.core.contstants.Constants.SHOP_EXPENSE
import com.miassolutions.milkledger.core.localdb.AppDatabase
import com.miassolutions.milkledger.core.localdb.expense.ExpenseDao
import com.miassolutions.milkledger.core.localdb.ledger.FinancialLedgerEntity
import com.miassolutions.milkledger.core.localdb.ledger.LedgerDao
import com.miassolutions.milkledger.core.localdb.ledger.LedgerEntryType
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
    private val db: AppDatabase
) {

    // ------------------------------------------------
    // 1️⃣ SAVE ALL (Batch Insert)
    // ------------------------------------------------
    suspend fun saveAllExpenses(expenses: List<Expense>) {
        db.withTransaction {
            val entities = expenses.map { it.toEntity() }
            expenseDao.insertAll(entities)

            val ledgerEntries = entities.map { expenseEntity ->
                // Logic Selection
                val isPersonal = expenseEntity.isPersonal
                val accountId = if (isPersonal) OWNER_ACCOUNT_ID else SHOP_EXPENSE
                val type = if (isPersonal) LedgerEntryType.OWNER_DRAWING else LedgerEntryType.BUSINESS_EXPENSE

                // Profit Impact Logic
                val profitImpact = if (isPersonal) 0L else -(expenseEntity.amount)

                FinancialLedgerEntity(
                    dateMillis = expenseEntity.dateMillis,
                    accountId = accountId,
                    referenceId = expenseEntity.expenseId,
                    type = type,

                    // 🔥 FIX: Expense = Debit (Paisa gya/Kharcha hoa)
                    debit = expenseEntity.amount,
                    credit = 0,

                    profitImpact = profitImpact,
                    note = expenseEntity.note ?: expenseEntity.title
                )
            }
            ledgerDao.insertAll(ledgerEntries)
        }
    }

    // ------------------------------------------------
    // 2️⃣ SAVE SINGLE (Single Insert)
    // ------------------------------------------------
    suspend fun saveExpense(expense: Expense) {
        db.withTransaction {
            // 1. Expense Table
            val expenseEntity = expense.toEntity()
            expenseDao.insertExpense(expenseEntity)

            // Logic Selection
            val isPersonal = expense.isPersonal
            val accountId = if (isPersonal) OWNER_ACCOUNT_ID else SHOP_EXPENSE
            val type = if (isPersonal) LedgerEntryType.OWNER_DRAWING else LedgerEntryType.BUSINESS_EXPENSE
            val profitImpact = if (isPersonal) 0L else -(expenseEntity.amount)

            // 2. Ledger Table
            val ledgerEntry = FinancialLedgerEntity(
                dateMillis = expense.date.toMillis(),
                accountId = accountId,
                referenceId = expenseEntity.expenseId,
                type = type,

                // 🔥 FIX: Expense = Debit
                debit = expenseEntity.amount,
                credit = 0,

                profitImpact = profitImpact,
                note = expense.title
            )
            ledgerDao.insert(ledgerEntry)
        }
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

                // Logic Re-Check (Agar user ne category change ki ho)
                val isPersonal = updatedExpense.isPersonal
                val newAccountId = if (isPersonal) OWNER_ACCOUNT_ID else SHOP_EXPENSE
                val newType = if (isPersonal) LedgerEntryType.OWNER_DRAWING else LedgerEntryType.BUSINESS_EXPENSE
                val newProfitImpact = if (isPersonal) 0L else -(updatedExpense.amount)

                val newLedgerEntry = entry.copy(
                    dateMillis = updatedExpense.date.toMillis(), // Date bhi update hoskti hy

                    accountId = newAccountId, // 🔥 Account ID bhi update karein
                    type = newType,           // 🔥 Type bhi update karein

                    debit = updatedExpense.amount, // Amount update
                    credit = 0,

                    profitImpact = newProfitImpact,
                    note = updatedExpense.title,

                    isSynced = false,
                    updatedAtMillis = System.currentTimeMillis()
                )

                ledgerDao.update(newLedgerEntry)
            }
        }
    }

    // ------------------------------------------------
    // 4️⃣ DELETE
    // ------------------------------------------------
    suspend fun deleteExpense(expenseId: String) {
        db.withTransaction {
            val currentTime = System.currentTimeMillis()
            expenseDao.softDeleteExpense(expenseId, currentTime)
            ledgerDao.softDeleteByReference(expenseId, currentTime)
        }
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
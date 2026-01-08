package com.miassolutions.milkledger.features.expense.data.repository

import androidx.room.withTransaction
import com.miassolutions.milkledger.core.contstants.Constants
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
    private val ledgerDao: LedgerDao,  // ✅ Added for Double Entry
    private val db: AppDatabase        // ✅ Added for Atomic Transactions
) {

    // ------------------------------------------------
    // 1️⃣ SAVE (Expense + Ledger Update)
    // ------------------------------------------------

    suspend fun saveAllExpenses(expenses: List<Expense>) {
        db.withTransaction {
            val entities = expenses.map { it.toEntity() }
            expenseDao.insertAll(entities)

            val ledgerEntries = entities.map { expenseEntity ->
                FinancialLedgerEntity(
                    dateMillis = expenseEntity.dateMillis,
                    accountId = if (expenseEntity.isPersonal) OWNER_ACCOUNT_ID else SHOP_EXPENSE,
                    referenceId = expenseEntity.expenseId,
                    type = if (expenseEntity.isPersonal) LedgerEntryType.OWNER_WITHDRAWAL else LedgerEntryType.EXPENSE,

                    debit = 0,
                    credit = expenseEntity.amount,

                    profitImpact = if (expenseEntity.isPersonal) 0 else -(expenseEntity.amount),
                    note = expenseEntity.note
                )
            }
            ledgerDao.insertAll(ledgerEntries)
        }
    }


    suspend fun saveExpense(expense: Expense) {
        db.withTransaction {
            // Step 1: Expense Table me save karein
            val expenseEntity = expense.toEntity() // Aapka LocalDate wala Mapper use ho rha hy
            expenseDao.insertExpense(expenseEntity)

            // Step 2: Ledger Table me entry dalein (Taake hisaab barabar rahe)
            val ledgerEntry = FinancialLedgerEntity(
                dateMillis = expense.date.toMillis(),

                // Expense kisi specific Account ka nahi hota (usually),
                // ya agar "Owner" ka withdrawal hai to accountId Owner ka hoga.
                // Filhal hum generic rakh rahe hain, ya aap Owner ID pass kar sakte hain.
                accountId = if (expense.isPersonal) OWNER_ACCOUNT_ID else SHOP_EXPENSE,

                referenceId = expenseEntity.expenseId, // Link to Expense
                type = if (expense.isPersonal) LedgerEntryType.OWNER_WITHDRAWAL else LedgerEntryType.EXPENSE,

                debit = 0,
                credit = expenseEntity.amount, // Paisa ja raha hai (Credit)

                // Profit logic: Personal withdrawal profit kam nahi karta, Business expense karta hai
                profitImpact = if (expense.isPersonal) 0 else -(expenseEntity.amount),

                note = expense.title
            )

            // Ledger DAO me Insert/Update (Ref ID ki base par check kar lega agar logic likhi ho)
            // Note: LedgerDao me humne 'getByReferenceId' banaya tha, us logic ko use kr k update kr skty hen
            // lekin filhal simple insert/replace:
            ledgerDao.insert(ledgerEntry)
        }
    }

    // ------------------------------------------------
    // 4️⃣ UPDATE (Single Expense + Ledger)
    // ------------------------------------------------
    suspend fun updateExpense(updatedExpense: Expense) {
        db.withTransaction {
            // 1. Expense Table Update
            expenseDao.updateExpense(updatedExpense.toEntity())

            // 2. Ledger Update (Reference ID se dhoond kar)
            val oldLedgerEntry = ledgerDao.getByReferenceId(updatedExpense.expenseId)

            oldLedgerEntry?.let { entry ->
                val newLedgerEntry = entry.copy(
                    // Amount update karein
                    credit = updatedExpense.amount,

                    // Agar Personal hai to Profit 0, warna Expense amount minus hogi
                    profitImpact = if (updatedExpense.isPersonal) 0 else -(updatedExpense.amount),

                    // Agar Title ya Note change hua ho
                    note = updatedExpense.title,


                    // Sync status reset karein taake Cloud pe bhi update ho
                    isSynced = false,
                    updatedAtMillis = System.currentTimeMillis()
                )

                ledgerDao.update(newLedgerEntry)
            }
        }
    }

    // ------------------------------------------------
    // 2️⃣ DELETE (Soft Delete Both)
    // ------------------------------------------------
    suspend fun deleteExpense(expenseId: String) {
        db.withTransaction {
            val currentTime = System.currentTimeMillis()

            // 1. Expense ko soft delete karein
            expenseDao.softDeleteExpense(expenseId, currentTime)
            // 2. Ledger Table se bhi Soft Delete (Direct Query)
            ledgerDao.softDeleteByReference(expenseId, currentTime)


        }
    }

    // ------------------------------------------------
    // 3️⃣ READ (Using LocalDate Mapper)
    // ------------------------------------------------

    fun getExpensesByDateRange(start: Long, end: Long): Flow<List<Expense>> {
        return expenseDao.getExpensesByDateRange(start, end).map { entities ->
            entities.map { it.toDomain() } // ✅ Entity -> Domain (LocalDate)
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

    // Reports
    fun getTotalExpenseAmount(start: Long, end: Long): Flow<Long> {
        return expenseDao.getTotalExpenseAmount(start, end).map { total ->
            total?.toLong() ?: 0L
        }
    }
}
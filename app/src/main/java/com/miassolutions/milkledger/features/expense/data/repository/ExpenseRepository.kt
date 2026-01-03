package com.miassolutions.milkledger.features.expense.data.repository

import com.miassolutions.milkledger.core.localdb.expense.ExpenseDao
import com.miassolutions.milkledger.features.expense.data.mapper.toEntity
import com.miassolutions.milkledger.features.expense.domain.Expense
import com.miassolutions.milkledger.features.transaction.data.Transaction
import com.miassolutions.milkledger.features.transaction.data.TransactionDao
import com.miassolutions.milkledger.features.transaction.data.TransactionEntity
import com.miassolutions.milkledger.features.transaction.data.TransactionType
import com.miassolutions.milkledger.features.transaction.data.toDomain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExpenseRepository @Inject constructor(
    private val expenseDao: ExpenseDao,
    private val transactionDao: TransactionDao,

    ) {


    suspend fun insertExpense(expense: Expense) {
        val entity = expense.toEntity()

        // 1️⃣ Save expense
        expenseDao.insertExpense(entity)

        // 2️⃣ Ledger entry (EXPENSE = money OUT)
        transactionDao.insert(
            TransactionEntity(
                dateMillis = entity.dateMillis,
                type = TransactionType.EXPENSE,
                referenceId = entity.expenseId,
                debit = entity.amount.toDouble(),
                credit = 0.0,
                profitImpact = -entity.amount.toDouble(),
                note = entity.title,
                accountId = "accountId" //todo()

            )
        )

    }

    suspend fun updateExpense(expense: Expense) {
        val entity = expense.toEntity()

        expenseDao.updateExpense(entity)

        // Append-only ledger
        transactionDao.insert(
            TransactionEntity(
                dateMillis = entity.dateMillis,
                type = TransactionType.EXPENSE,
                referenceId = entity.expenseId,
                debit = entity.amount.toDouble(),
                credit = 0.0,
                profitImpact = -entity.amount.toDouble(),
                note = "Expense updated",
                accountId = "accountId" //todo()
            )
        )
    }

    suspend fun deleteExpense(expenseId: String) {
        val deletedAt = System.currentTimeMillis()

        expenseDao.softDeleteExpense(expenseId, deletedAt)

        // 🆕 Ledger reversal
        transactionDao.insert(
            TransactionEntity(
                dateMillis = deletedAt,
                type = TransactionType.EXPENSE_REVERSAL,
                referenceId = expenseId,
                debit = 0.0,
                credit = 0.0,
                profitImpact = 0.0,
                note = "Expense deleted",
                accountId = "accountId" //todo()
            )
        )


    }

    /* ---------------------------------------------------
       ADJUSTMENTS
    --------------------------------------------------- */

    suspend fun addExpenseAdjustment(
        expenseId: String,
        amount: Double,
        note: String?
    ) {
        if (amount == 0.0) return
        if (expenseDao.getExpenseById(expenseId) == null) return

//        transactionDao.insert(
//            TransactionEntity(
//                dateMillis = System.currentTimeMillis(),
//                type = TransactionType.PROFIT_ADJUSTMENT,
//                referenceId = expenseId,
//                debit = if (amount > 0) amount else 0.0,
//                credit = if (amount < 0) -amount else 0.0,
//                profitImpact = -amount,
//                note = note
//            )
//        )
    }

    fun observeExpenseAdjustments(expenseId: String): Flow<List<Transaction>> =
        transactionDao.getAdjustmentsFor(expenseId)
            .map { list -> list.map { it.toDomain() } }


}

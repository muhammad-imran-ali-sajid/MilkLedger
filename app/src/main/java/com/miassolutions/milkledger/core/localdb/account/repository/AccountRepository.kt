package com.miassolutions.milkledger.core.localdb.account.repository

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
import com.miassolutions.milkledger.features.account.model.AccountUi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

// @Inject constructor zaroori hai taake Hilt isay pehchan sake
class AccountRepository @Inject constructor(
    private val dao: AccountDao,
    private val ledgerDao: LedgerDao, //  Added: Opening Balance k liye
    private val db: AppDatabase
) {

    suspend fun getOwner(): AccountEntity? =
        dao.getOwner()

    suspend fun saveOwner(owner: AccountEntity) {
        dao.upsert(owner)
    }

    // 1. Get List (Reactive Flow)
    fun getAccountsByType(type: AccountType): Flow<List<Account>> {
        return dao.getAccountsByType(type).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun isSortOrderExist(
        sortOrder: Int,
        accountType: AccountType,
        excludeId: String? = null
    ): Boolean = dao.isSortOrderExist(sortOrder, accountType, excludeId)

    // 2. Get Single Account
    suspend fun getAccountById(id: String): Account? {
        return dao.getAccountById(id)?.toDomain()
    }

    // 3. Save (Insert / Update)
    // Advance Amount yahan save ho jaye gi, lekin Ledger update nahi hoga (As per requirement)
    suspend fun saveAccount(account: Account) {
        val entity = account.toEntity()

        // Step A: Account Table me Save/Update
        dao.insert(entity)

        // step B: ledger logic(opening balance)
        // pehly puran opening balance del kre agr edit ho raha ho

        ledgerDao.deleteOpeningBalance(entity.accountId)

        // agar initial bal. 0 se zyada hy to nayi entry kren
        if (entity.initialBalance != null && entity.initialBalance > 0) {
            // Logic:
            // Customer ka Balance = DEBIT (Usne humein dene hain - Asset)
            // Supplier ka Balance = CREDIT (Humne usay dene hain - Liability)

            val isCustomer = entity.accountType == AccountType.CUSTOMER
            val debitAmount = if (isCustomer) entity.initialBalance else 0L
            val creditAmount = if (!isCustomer) entity.initialBalance else 0L

            val openingEntry = FinancialLedgerEntity(
                accountId = entity.accountId,
                dateMillis = entity.createdAtMillis, // Account banne ki tareekh
                referenceId = null, // Opening balance ka koi specific ref nahi hota
                type = LedgerEntryType.OPENING_BALANCE,

                debit = debitAmount,
                credit = creditAmount,

                profitImpact = 0, // Opening balance aaj ka profit nahi hai
                note = "Opening Balance"
            )

            ledgerDao.insert(openingEntry)
        }

    }

    // 4. Soft Delete
    suspend fun deleteAccount(accountId: String) {
        db.withTransaction {
            val currentTime = System.currentTimeMillis()

            // Account Soft Delete
            dao.softDelete(accountId, currentTime)

            // Uska Opening Balance bhi delete kar dein Ledger se
            ledgerDao.deleteOpeningBalance(accountId)
        }
    }

    suspend fun restoreAccount(accountUi: String) {
        dao.restore(accountUi)
    }

    suspend fun permanentlyDeleteAllSoftDeletedAccounts() {
        dao.permanentlyDeleteAllAccounts()
    }

//    // 5. Update Sort Order (Drag & Drop)
//    suspend fun updateSortOrder(accountId: String, newOrder: Int) {
//        dao.updateSortOrder(accountId, newOrder)
//    }
}
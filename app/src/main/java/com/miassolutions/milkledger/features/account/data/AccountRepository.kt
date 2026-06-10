package com.miassolutions.milkledger.features.account.data

import androidx.room.withTransaction
import com.miassolutions.milkledger.core.localdb.database.AppDatabase
import com.miassolutions.milkledger.core.localdb.account.local.AccountDao
import com.miassolutions.milkledger.core.localdb.account.local.AccountType
import com.miassolutions.milkledger.core.localdb.account.local.AccountWithStats
import com.miassolutions.milkledger.core.localdb.account.local.toDomain
import com.miassolutions.milkledger.core.localdb.account.local.toEntity
import com.miassolutions.milkledger.core.localdb.ledger.FinancialLedgerEntity
import com.miassolutions.milkledger.core.localdb.ledger.LedgerDao
import com.miassolutions.milkledger.core.localdb.ledger.LedgerEntryType
import com.miassolutions.milkledger.features.account.domain.Account
import com.miassolutions.milkledger.features.backup.data.BackupRepository
import com.miassolutions.milkledger.utils.extensions.toLocalDate
import com.miassolutions.milkledger.utils.extensions.toMillis
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject

class AccountRepository @Inject constructor(
    private val accountDao: AccountDao,
    private val ledgerDao: LedgerDao,
    private val backupRepository: BackupRepository,
    private val db: AppDatabase
) {
    
    /* ---------------- READ ---------------- */
    
    // 1. Purana function (Simple List)
    fun getAccountsByType(type: AccountType): Flow<List<Account>> {
        return accountDao.getAccountsByType(type)
            .map { entities ->
                entities.map { it.toDomain() }
            }
    }
    
    // 🔥 2. Naya OPTIMIZED Function (List + Balance + Date)
    // Ye ViewModel me use hoga taake N+1 query problem khatam ho jaye
    fun getAccountsWithStats(type: AccountType): Flow<List<AccountWithStats>> {
        return accountDao.getAccountsWithStats(type)
    }
    
    suspend fun getAccountById(accountId: String): Account? =
        accountDao.getAccountById(accountId)?.toDomain()
    
    suspend fun isSortOrderExist(
        sortOrder: Int,
        type: AccountType,
        excludeId: String?
    ): Boolean =
        accountDao.isSortOrderExist(sortOrder, type, excludeId)
    
    suspend fun getOpeningDate(accountId: String): LocalDate? =
        ledgerDao.getOpeningBalanceEntry(accountId)
            ?.dateMillis
            ?.toLocalDate()
    
    suspend fun getCurrentBalance(accountId: String): Long =
        ledgerDao.getAccountNetBalance(accountId) ?: 0L
    
    /* ---------------- WRITE (Business-safe) ---------------- */
    
    suspend fun saveAccount(account: Account, openingDate: LocalDate) {
        
        val entity = account.toEntity()
        
        db.withTransaction {
            
            // 1️⃣ Account UPSERT
            accountDao.insert(entity)
            
            // 2️⃣ Opening Balance Ledger
            val existing = ledgerDao.getOpeningBalanceEntry(entity.accountId)
            
            val balance = entity.initialBalance ?: 0L
            val isCustomer = entity.accountType == AccountType.CUSTOMER
            
            val debit = when {
                isCustomer && balance > 0 -> balance
                !isCustomer && balance < 0 -> -balance
                else -> 0L
            }
            
            val credit = when {
                isCustomer && balance < 0 -> -balance
                !isCustomer && balance > 0 -> balance
                else -> 0L
            }
            
            if (existing != null) {
                ledgerDao.update(
                    existing.copy(
                        dateMillis = openingDate.toMillis(),
                        debit = debit,
                        credit = credit,
                        updatedAtMillis = System.currentTimeMillis()
                    )
                )
            } else {
                ledgerDao.insert(
                    FinancialLedgerEntity(
                        accountId = entity.accountId,
                        dateMillis = openingDate.toMillis(),
                        referenceId = entity.accountId,
                        type = LedgerEntryType.OPENING_BALANCE,
                        debit = debit,
                        credit = credit,
                        profitImpact = 0,
                        note = "Opening Balance"
                    )
                )
            }
        }
        backupRepository.markDataChanged()
    }
    
    suspend fun deleteAccount(accountId: String) {
        db.withTransaction {
            val now = System.currentTimeMillis()
            accountDao.softDelete(accountId, now)
            ledgerDao.softDeleteOpeningBalance(accountId, now)
        }
        backupRepository.markDataChanged()
    }
}
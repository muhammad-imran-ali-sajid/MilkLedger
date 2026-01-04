package com.miassolutions.milkledger.core.localdb.account.repository

import com.miassolutions.milkledger.core.localdb.account.local.AccountDao
import com.miassolutions.milkledger.core.localdb.account.local.AccountType
import com.miassolutions.milkledger.core.localdb.account.local.toDomain
import com.miassolutions.milkledger.core.localdb.account.local.toEntity
import com.miassolutions.milkledger.features.account.domain.Account
import com.miassolutions.milkledger.features.account.model.AccountUi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

// @Inject constructor zaroori hai taake Hilt isay pehchan sake
class AccountRepository @Inject constructor(
    private val dao: AccountDao
) {

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
        dao.insert(account.toEntity())
    }

    // 4. Soft Delete
    suspend fun deleteAccount(accountId: String) {
        dao.softDelete(accountId, System.currentTimeMillis())
    }

    suspend fun restoreAccount(accountUi: String) {
        dao.restore(accountUi)
    }

//    // 5. Update Sort Order (Drag & Drop)
//    suspend fun updateSortOrder(accountId: String, newOrder: Int) {
//        dao.updateSortOrder(accountId, newOrder)
//    }
}
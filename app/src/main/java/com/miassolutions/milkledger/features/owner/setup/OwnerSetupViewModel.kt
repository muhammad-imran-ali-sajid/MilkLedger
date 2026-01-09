package com.miassolutions.milkledger.features.owner.setup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miassolutions.milkledger.core.contstants.Constants.OWNER_ACCOUNT_ID
import com.miassolutions.milkledger.core.localdb.account.local.AccountEntity
import com.miassolutions.milkledger.core.localdb.account.local.AccountType
import com.miassolutions.milkledger.core.localdb.account.repository.AccountRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OwnerSetupViewModel @Inject constructor(
    private val repository: AccountRepository
) : ViewModel() {

    fun saveOwner(
        name: String,
        phone: String

    ) {
        val owner = AccountEntity(
            accountId = OWNER_ACCOUNT_ID, // 🔒 FIXED
            name = name,
            phone = phone,
            accountType = AccountType.OWNER,
            defaultRate = 0.0,
            initialBalance = 0,
            advanceAmount = null,
            sortOrder = 0
        )

        viewModelScope.launch {
            repository.saveOwner(owner)
        }
    }
}

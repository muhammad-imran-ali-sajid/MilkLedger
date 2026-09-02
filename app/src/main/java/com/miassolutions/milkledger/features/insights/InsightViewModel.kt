package com.miassolutions.milkledger.features.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miassolutions.milkledger.core.localdb.account.local.AccountType
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn


@HiltViewModel
class InsightViewModel @Inject constructor(
    private val repository: InsightRepository
) : ViewModel() {

    private val _currentAccountType = MutableStateFlow(AccountType.CUSTOMER)

    @OptIn(ExperimentalCoroutinesApi::class)
    val balanceInsight: StateFlow<List<BalanceWithAccountType>> = _currentAccountType
        .flatMapLatest { type ->
            repository.getBalanceList(type)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    fun setAccountType(type: AccountType) {
        _currentAccountType.value = type
    }

}
package com.miassolutions.milkledger.features.account.list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miassolutions.milkledger.core.localdb.account.local.AccountType
import com.miassolutions.milkledger.core.localdb.account.repository.AccountRepository
import com.miassolutions.milkledger.features.account.mapper.toUiList
import com.miassolutions.milkledger.features.account.mapper.toUiListFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val repository: AccountRepository,
    private val savedStateHandle: SavedStateHandle
) :
    ViewModel() {

    private companion object {
        const val KEY_SELECTED_TAB = "selected_tab"
    }


    private val _selectedTab =
        MutableStateFlow(savedStateHandle[KEY_SELECTED_TAB] ?: AccountType.CUSTOMER)

    val selectedTab = _selectedTab.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val accounts = _selectedTab
        .flatMapLatest { type ->
            repository.getAccountsByType(type).toUiListFlow()

        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onTabSelected(type: AccountType) {
        _selectedTab.value = type
        savedStateHandle[KEY_SELECTED_TAB] = type
    }

    fun delete(id: String) {
        viewModelScope.launch {
            repository.deleteAccount(id)
        }
    }

    fun restore(id: String) {
        viewModelScope.launch {
            repository.restoreAccount(id)
        }
    }

    fun permanentlyDeleteSoftDeleted() {
        viewModelScope.launch {

            repository.permanentlyDeleteAllSoftDeletedAccounts()
        }
    }


}
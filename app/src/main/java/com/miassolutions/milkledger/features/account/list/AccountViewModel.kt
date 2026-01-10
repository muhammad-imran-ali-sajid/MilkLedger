package com.miassolutions.milkledger.features.account.list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miassolutions.milkledger.core.localdb.account.local.AccountType
import com.miassolutions.milkledger.features.account.data.AccountRepository
import com.miassolutions.milkledger.features.account.mapper.toUiListFlow
import com.miassolutions.milkledger.features.account.model.AccountUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val repository: AccountRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    companion object {
        private const val KEY_SELECTED_TAB = "selected_tab"
        private const val KEY_VISIBILITY = "visibility"
    }

    /* --------------------------------------------------
     * TAB = Account TYPE (Primary)
     * -------------------------------------------------- */

    private val _selectedTab = MutableStateFlow(
        savedStateHandle[KEY_SELECTED_TAB] ?: AccountType.CUSTOMER
    )
    val selectedTab = _selectedTab.asStateFlow()

    fun onTabSelected(type: AccountType) {
        _selectedTab.value = type
        savedStateHandle[KEY_SELECTED_TAB] = type
    }

    /* --------------------------------------------------
     * FILTER = Account STATUS (Secondary)
     * -------------------------------------------------- */

    enum class AccountVisibility {
        ACTIVE_ONLY,
        ALL
    }

    private val _visibility = MutableStateFlow(
        savedStateHandle[KEY_VISIBILITY] ?: AccountVisibility.ACTIVE_ONLY
    )
    val visibility = _visibility.asStateFlow()

    fun toggleVisibility() {
        val newValue =
            if (_visibility.value == AccountVisibility.ACTIVE_ONLY)
                AccountVisibility.ALL
            else
                AccountVisibility.ACTIVE_ONLY

        _visibility.value = newValue
        savedStateHandle[KEY_VISIBILITY] = newValue
    }

    /* --------------------------------------------------
     * FINAL ACCOUNTS LIST (Tab + Filter combined)
     * -------------------------------------------------- */

    @OptIn(ExperimentalCoroutinesApi::class)
    val accounts: StateFlow<List<AccountUi>> =
        combine(
            selectedTab,
            visibility
        ) { type, visibility ->
            type to visibility
        }.flatMapLatest { (type, visibility) ->

            repository.getAccountsByType(type)
                .map { list ->
                    when (visibility) {
                        AccountVisibility.ACTIVE_ONLY ->
                            list.filter { it.isActive }

                        AccountVisibility.ALL ->
                            list
                    }
                }
                .toUiListFlow()

        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )
}

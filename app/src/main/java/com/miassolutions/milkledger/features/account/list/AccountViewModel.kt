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

    /* -------------------------------------------------- */
    /* PRIMARY FILTER: ACCOUNT TYPE (TAB)                 */
    /* -------------------------------------------------- */

    private val _selectedTab = MutableStateFlow(
        savedStateHandle[KEY_SELECTED_TAB] ?: AccountType.SUPPLIER // ✅ DEFAULT
    )
    val selectedTab: StateFlow<AccountType> = _selectedTab.asStateFlow()

    fun onTabSelected(type: AccountType) {
        _selectedTab.value = type
        savedStateHandle[KEY_SELECTED_TAB] = type
    }

    /* -------------------------------------------------- */
    /* SECONDARY FILTER: VISIBILITY                       */
    /* -------------------------------------------------- */

    enum class AccountVisibility {
        ACTIVE_ONLY,
        ALL
    }

    private val _visibility = MutableStateFlow(
        savedStateHandle[KEY_VISIBILITY] ?: AccountVisibility.ACTIVE_ONLY
    )
    val visibility: StateFlow<AccountVisibility> = _visibility.asStateFlow()

    fun toggleVisibility() {
        val next =
            if (_visibility.value == AccountVisibility.ACTIVE_ONLY)
                AccountVisibility.ALL
            else
                AccountVisibility.ACTIVE_ONLY

        _visibility.value = next
        savedStateHandle[KEY_VISIBILITY] = next
    }

    /* -------------------------------------------------- */
    /* FINAL LIST (TAB + FILTER COMBINED)                 */
    /* -------------------------------------------------- */

    @OptIn(ExperimentalCoroutinesApi::class)
    val accounts: StateFlow<List<AccountUi>> =
        combine(selectedTab, visibility) { type, visibility ->
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

package com.miassolutions.milkledger.features.account.list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miassolutions.milkledger.core.localdb.account.local.AccountType
import com.miassolutions.milkledger.features.account.data.AccountRepository
import com.miassolutions.milkledger.features.account.model.AccountUi
import com.miassolutions.milkledger.utils.extensions.toLocalDate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
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

    enum class AccountVisibility {
        ACTIVE_ONLY,
        ALL
    }

    private val _selectedTab = MutableStateFlow(readSavedAccountType())
    val selectedTab: StateFlow<AccountType> = _selectedTab.asStateFlow()

    private val _visibility = MutableStateFlow(readSavedVisibility())
    val visibility: StateFlow<AccountVisibility> = _visibility.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val accounts: StateFlow<List<AccountUi>> =
        combine(
            selectedTab,
            visibility
        ) { type, visibility ->
            type to visibility
        }.flatMapLatest { (type, visibility) ->

            repository.getAccountsWithStats(type)
                .map { list ->

                    list
                        .asSequence()
                        .filter { item ->
                            when (visibility) {
                                AccountVisibility.ACTIVE_ONLY -> item.account.isActive
                                AccountVisibility.ALL -> true
                            }
                        }
                        .sortedWith(
                            compareBy(
                                { it.account.sortOrder },
                                { it.account.name.lowercase() }
                            )
                        )
                        .map { item ->
                            val entity = item.account

                            AccountUi(
                                id = entity.accountId,
                                name = entity.name,
                                type = entity.accountType,
                                sortOrder = entity.sortOrder,
                                initialBalance = entity.initialBalance,
                                currentBalance = item.currentBalance ?: 0L,
                                openingDate = item.openingDateMillis?.toLocalDate()
                                    ?: LocalDate.now(),
                                defaultRate = entity.defaultRate,
                                advanceAmount = entity.advanceAmount
                            )
                        }
                        .toList()
                }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun onTabSelected(type: AccountType) {
        if (_selectedTab.value == type) return

        _selectedTab.value = type
        savedStateHandle[KEY_SELECTED_TAB] = type.name
    }

    fun setIncludeArchived(includeArchived: Boolean) {
        val next =
            if (includeArchived) {
                AccountVisibility.ALL
            } else {
                AccountVisibility.ACTIVE_ONLY
            }

        if (_visibility.value == next) return

        _visibility.value = next
        savedStateHandle[KEY_VISIBILITY] = next.name
    }

    fun toggleVisibility() {
        setIncludeArchived(
            includeArchived = _visibility.value == AccountVisibility.ACTIVE_ONLY
        )
    }

    private fun readSavedAccountType(): AccountType {
        val saved = savedStateHandle.get<String>(KEY_SELECTED_TAB)

        return runCatching {
            saved?.let { AccountType.valueOf(it) }
        }.getOrNull() ?: AccountType.SUPPLIER
    }

    private fun readSavedVisibility(): AccountVisibility {
        val saved = savedStateHandle.get<String>(KEY_VISIBILITY)

        return runCatching {
            saved?.let { AccountVisibility.valueOf(it) }
        }.getOrNull() ?: AccountVisibility.ACTIVE_ONLY
    }
}
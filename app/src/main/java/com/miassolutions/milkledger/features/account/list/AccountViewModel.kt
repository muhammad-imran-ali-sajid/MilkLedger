package com.miassolutions.milkledger.features.account.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class AccountViewModel : ViewModel() {

    private val allItems = listOf(
        Item(1, "Item A", ItemType.FIRST),
        Item(2, "Item B", ItemType.SECOND),
        Item(3, "Item C", ItemType.FIRST),
        Item(4, "Item D", ItemType.SECOND)
    )


    private val _selectedTab = MutableStateFlow(ItemType.FIRST)

    val items = _selectedTab.map { type ->
        val accounts = allItems.map { it.toUi() }
        accounts.filter { it.typeLabel == type.name }

    }.stateIn(viewModelScope, SharingStarted.Companion.WhileSubscribed(5000), emptyList())

    fun onTabSelected(type: ItemType) {
        _selectedTab.value = type
    }
}
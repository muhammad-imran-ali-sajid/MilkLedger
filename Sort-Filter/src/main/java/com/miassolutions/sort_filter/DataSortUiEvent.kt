package com.miassolutions.sort_filter

sealed interface DataSortUiEvent {
    data class FilterChanged(val id: String) : DataSortUiEvent
    data class SortChanged(val id: String) : DataSortUiEvent
    data object ApplyClicked : DataSortUiEvent
    data object ResetClicked : DataSortUiEvent
}
package com.miassolutions.sort_filter



data class DataSortUiState(
    val filterOptions: List<FilterOption> = emptyList(),
    val sortOptions: List<SortOption> = emptyList()
)

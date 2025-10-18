package com.miassolutions.milkledger.core.ui.sort



data class FilterOptions(
    val sortOrder: SortOrder = SortOrder.NONE,
    val category: String? = null
)

enum class SortOrder {
    NONE, ASCENDING, DESCENDING
}

package com.miassolutions.milkledger.core.ui

import java.util.*

class FilterableDelegate<T>(
    private val adapter: androidx.recyclerview.widget.ListAdapter<T, *>,
    private val matcher: (T, String) -> Boolean
) : FilterableList<T> {

    private var originalList: List<T> = emptyList()
    private var currentQuery: String = ""

    override fun setOriginalList(list: List<T>) {
        originalList = list
        filter(currentQuery)
    }

    override fun filter(query: String) {
        currentQuery = query.lowercase(Locale.getDefault())
        val filtered = if (query.isBlank()) {
            originalList
        } else {
            originalList.filter { matcher(it, currentQuery) }
        }
        adapter.submitList(filtered)
    }
}

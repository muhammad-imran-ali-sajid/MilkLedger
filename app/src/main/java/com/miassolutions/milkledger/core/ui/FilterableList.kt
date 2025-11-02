package com.miassolutions.milkledger.core.ui

interface FilterableList<T> {
    fun setOriginalList(list: List<T>)
    fun filter(query: String)
}

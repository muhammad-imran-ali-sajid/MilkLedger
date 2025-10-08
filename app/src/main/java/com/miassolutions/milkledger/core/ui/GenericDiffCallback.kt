package com.miassolutions.milkledger.core.ui

import androidx.recyclerview.widget.DiffUtil


class GenericDiffCallback<T : Any>(
    private val areItemsSame: (oldItem: T, newItem: T) -> Boolean,
    private val areContentsSame: (oldItem: T, newItem: T) -> Boolean = { old, new -> old == new }
) : DiffUtil.ItemCallback<T>() {
    override fun areItemsTheSame(oldItem: T, newItem: T): Boolean = areItemsSame(oldItem, newItem)
    override fun areContentsTheSame(oldItem: T, newItem: T): Boolean = areContentsSame(oldItem, newItem)
}

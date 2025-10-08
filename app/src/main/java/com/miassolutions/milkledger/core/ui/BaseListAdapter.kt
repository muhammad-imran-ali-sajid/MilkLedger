package com.miassolutions.milkledger.core.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

/**
 * BaseListAdapter — reusable and type-safe RecyclerView adapter.
 *
 * Features:
 * ✅ Works with any data model (T)
 * ✅ Uses ViewBinding (VB)
 * ✅ Optional onItemClick / onItemLongClick
 * ✅ Uses DiffUtil for efficient updates
 */
abstract class BaseListAdapter<T, VB : ViewBinding>(
    diffCallback: DiffUtil.ItemCallback<T>,
    private val onItemClick: ((T) -> Unit)? = null,
    private val onItemLongClick: ((T) -> Boolean)? = null,
    private val inflate: (LayoutInflater, ViewGroup, Boolean) -> VB
) : ListAdapter<T, BaseListAdapter.BaseViewHolder<VB>>(diffCallback) {

    abstract fun createBinding(inflater: LayoutInflater, parent: ViewGroup): VB
    abstract fun bind(binding: VB, item: T, position: Int)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<VB> {
        val inflater = LayoutInflater.from(parent.context)
        val binding = createBinding(inflater, parent)
        return BaseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BaseViewHolder<VB>, position: Int) {
        val item = getItem(position)
        bind(holder.binding, item, position)

        holder.binding.root.setOnClickListener {
            onItemClick?.invoke(item)
        }

        holder.binding.root.setOnLongClickListener {
            onItemLongClick?.invoke(item) ?: false
        }
    }

    class BaseViewHolder<VB : ViewBinding>(val binding: VB) :
        RecyclerView.ViewHolder(binding.root)
}






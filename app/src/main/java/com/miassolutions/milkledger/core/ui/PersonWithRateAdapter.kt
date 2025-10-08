package com.miassolutions.milkledger.core.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.miassolutions.milkledger.databinding.ItemCustomerBinding

class PersonWithRateAdapter<T : PersonWithRate>(
    private val onItemClick: (T) -> Unit
) : ListAdapter<T, PersonWithRateAdapter<T>.PersonViewHolder>(GenericDiffCallback<T>()) {

    inner class PersonViewHolder(private val binding: ItemCustomerBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: T) = with(binding) {
            tvCustomerName.text = item.name
            tvCustomerRate.text = "Rs%.2f".format(item.rate)

            root.setOnClickListener {
                onItemClick(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PersonViewHolder {
        val binding = ItemCustomerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PersonViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PersonViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class GenericDiffCallback<T : PersonWithRate> : DiffUtil.ItemCallback<T>() {
    override fun areItemsTheSame(oldItem: T, newItem: T): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: T, newItem: T): Boolean {
        return oldItem == newItem
    }
}


package com.miassolutions.milkledger.presentation.customer.customerslist

import android.transition.AutoTransition
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.miassolutions.milkledger.databinding.ItemCustomerBinding
import com.miassolutions.milkledger.presentation.customer.model.CustomerUi

class CustomerListAdapter(
    private val onEditClick: (String) -> Boolean
) : ListAdapter<CustomerUi, CustomerListAdapter.CustomerViewHolder>(CustomerDiffCallback()) {

    inner class CustomerViewHolder(
        private val binding: ItemCustomerBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(customer: CustomerUi) = with(binding) {

            // Bind main info
            tvCustomerName.text = customer.name
            tvPosition.text = customer.sortOrder.toString()
            tvCustomerRate.text = "Rs. ${customer.rate}"
            tvAdvanceAmount.text = "Rs. ${customer.advanceAmount}"


            layoutExpandable.visibility = if (customer.isExpanded) View.VISIBLE else View.GONE
            imgArrow.rotation = if (customer.isExpanded) 180f else 0f

            // Set click listeners
            root.setOnClickListener {
                val newList = currentList.toMutableList()
                val updated = customer.copy(isExpanded = !customer.isExpanded)
                newList[absoluteAdapterPosition] = updated

                // Smooth expand/collapse animation
                TransitionManager.beginDelayedTransition(root as ViewGroup, AutoTransition())
                submitList(newList)
            }

            root.setOnLongClickListener {
                onEditClick(customer.id)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomerViewHolder {
        val binding = ItemCustomerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CustomerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CustomerViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class CustomerDiffCallback : DiffUtil.ItemCallback<CustomerUi>() {
    override fun areItemsTheSame(oldItem: CustomerUi, newItem: CustomerUi): Boolean =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: CustomerUi, newItem: CustomerUi): Boolean =
        oldItem == newItem
}

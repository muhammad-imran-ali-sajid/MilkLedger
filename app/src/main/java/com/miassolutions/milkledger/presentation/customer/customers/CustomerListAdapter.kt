package com.miassolutions.milkledger.presentation.customer.customers

import android.transition.AutoTransition
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.miassolutions.milkledger.databinding.ItemCustomerBinding
import com.miassolutions.milkledger.domain.model.Customer

class CustomerListAdapter(
    private val onEditClick: (Customer) -> Boolean
) : ListAdapter<Customer, CustomerListAdapter.CustomerViewHolder>(CustomerDiffCallback()) {

    inner class CustomerViewHolder(
        private val binding: ItemCustomerBinding
    ) : RecyclerView.ViewHolder(binding.root) {

//        fun bind(customer: Customer) = with(binding) {
//            // Bind main info
//            tvCustomerName.text = customer.name
//            tvPosition.text = "${customer.sortOrder}"
//            tvCustomerRate.text = "${"%.2f".format(customer.rate)}"
//            tvAdvanceAmount.text = "${"%.2f".format(customer.advanceAmount)}"
//
//            // Handle expand/collapse visibility
//            layoutExpandable.visibility = if (customer.isExpanded) View.VISIBLE else View.GONE
//            imgArrow.rotation = if (customer.isExpanded) 180f else 0f
//
//            // Set click listeners
//            root.setOnClickListener {
//                val newList = currentList.toMutableList()
//                val updated = customer.copy(isExpanded = !customer.isExpanded)
//                newList[absoluteAdapterPosition] = updated
//
//                // Smooth expand/collapse animation
//                TransitionManager.beginDelayedTransition(root as ViewGroup, AutoTransition())
//                submitList(newList)
//            }
//
//            root.setOnLongClickListener {
//                onEditClick(customer)
//            }
//        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomerViewHolder {
        val binding = ItemCustomerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CustomerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CustomerViewHolder, position: Int) {
//        holder.bind(getItem(position))
    }
}

class CustomerDiffCallback : DiffUtil.ItemCallback<Customer>() {
    override fun areItemsTheSame(oldItem: Customer, newItem: Customer): Boolean =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: Customer, newItem: Customer): Boolean =
        oldItem == newItem
}

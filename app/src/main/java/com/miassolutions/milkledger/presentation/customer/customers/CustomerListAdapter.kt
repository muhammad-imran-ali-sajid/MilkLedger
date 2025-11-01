package com.miassolutions.milkledger.presentation.customer.customers

import android.view.LayoutInflater
import android.view.ViewGroup
import com.miassolutions.milkledger.core.ui.BaseListAdapter
import com.miassolutions.milkledger.core.ui.GenericDiffCallback
import com.miassolutions.milkledger.databinding.ItemCustomerBinding
import com.miassolutions.milkledger.domain.model.Customer


class CustomerListAdapter(
    onItemLongClick: (Customer) -> Boolean
) : BaseListAdapter<Customer, ItemCustomerBinding>(
    inflate = ItemCustomerBinding::inflate,
    onItemLongClick = onItemLongClick,
    diffCallback = GenericDiffCallback(
        areItemsSame = { old, new -> old.id == new.id },
        areContentsSame = { old, new -> old == new }
    )
) {
    override fun bind(binding: ItemCustomerBinding, item: Customer, position: Int) = with(binding) {
        tvCustomerName.text = item.name
        tvCustomerRate.text = item.rate.let { "Rs %.2f".format(it) }
        tvPosition.text = item.sortOrder.toString()

    }

    override fun createBinding(inflater: LayoutInflater, parent: ViewGroup): ItemCustomerBinding =
        ItemCustomerBinding.inflate(inflater, parent, false)


}


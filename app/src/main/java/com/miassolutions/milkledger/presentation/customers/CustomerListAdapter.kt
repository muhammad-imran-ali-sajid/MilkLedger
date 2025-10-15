package com.miassolutions.milkledger.presentation.customers

import android.view.LayoutInflater
import android.view.ViewGroup
import com.miassolutions.milkledger.core.ui.BaseListAdapter
import com.miassolutions.milkledger.core.ui.GenericDiffCallback
import com.miassolutions.milkledger.databinding.ItemCustomerBinding
import com.miassolutions.milkledger.domain.model.Customer


class CustomerListAdapter(
    onItemClick: (Customer) -> Unit
) : BaseListAdapter<Customer, ItemCustomerBinding>(
    inflate = ItemCustomerBinding::inflate,
    onItemClick = onItemClick,
    diffCallback = GenericDiffCallback(
        areItemsSame = { old, new -> old.id == new.id },
        areContentsSame = { old, new -> old == new }
    )
) {
    override fun bind(binding: ItemCustomerBinding, item: Customer, position: Int) = with(binding) {
        tvCustomerName.text = item.name
        tvCustomerRate.text = item.rate.let { "Rs %.2f".format(it) }

    }

    override fun createBinding(inflater: LayoutInflater, parent: ViewGroup): ItemCustomerBinding =
        ItemCustomerBinding.inflate(inflater, parent, false)


}


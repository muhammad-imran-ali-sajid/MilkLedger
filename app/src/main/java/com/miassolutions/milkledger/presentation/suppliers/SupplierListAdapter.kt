package com.miassolutions.milkledger.presentation.suppliers

import android.view.LayoutInflater
import android.view.ViewGroup
import com.miassolutions.milkledger.core.ui.BaseListAdapter
import com.miassolutions.milkledger.core.ui.GenericDiffCallback
import com.miassolutions.milkledger.databinding.ItemSupplierBinding
import com.miassolutions.milkledger.domain.model.Supplier

class SupplierListAdapter() : BaseListAdapter<Supplier, ItemSupplierBinding>(
    inflate = ItemSupplierBinding::inflate,
    diffCallback = GenericDiffCallback(
        areItemsSame = { old, new -> old.id == new.id },
        areContentsSame = { old, new -> old == new }
    )
) {
    override fun createBinding(inflater: LayoutInflater, parent: ViewGroup): ItemSupplierBinding =
        ItemSupplierBinding.inflate(inflater, parent, false)

    override fun bind(binding: ItemSupplierBinding, item: Supplier, position: Int) =with(binding){
        tvSupplierName.text = item.name
        tvSupplierRate.text = item.rate.let { "Rs%.2f".format(it) }
    }
}

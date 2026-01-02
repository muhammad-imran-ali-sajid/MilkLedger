package com.miassolutions.milkledger.features.supplier.data.mapper

import com.miassolutions.milkledger.features.supplier.domain.Supplier
import com.miassolutions.milkledger.features.supplier.ui.model.SupplierUi


fun Supplier.toUi() = SupplierUi(
    id = id,
    name = name,
    rate = rate,
    sortOrder = sortOrder,
    advanceAmount = advanceAmount,
    isDefault = isDefault,
    isExpanded = false
)
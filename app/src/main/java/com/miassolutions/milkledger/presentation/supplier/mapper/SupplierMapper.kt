package com.miassolutions.milkledger.presentation.supplier.mapper

import com.miassolutions.milkledger.domain.model.Supplier
import com.miassolutions.milkledger.presentation.supplier.model.SupplierUi

fun Supplier.toUi() = SupplierUi(
    id = id,
    name = name,
    rate = rate,
    sortOrder = sortOrder,
    advanceAmount = advanceAmount,
    isDefault = isDefault,
    isExpanded = false
)
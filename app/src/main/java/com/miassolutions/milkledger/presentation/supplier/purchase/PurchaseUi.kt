package com.miassolutions.milkledger.presentation.supplier.purchase

import com.miassolutions.milkledger.data.local.relations.PurchaseWithSupplier

data class PurchaseUi(
    val data: PurchaseWithSupplier,
    val accumulatedBalance: Double
)


package com.miassolutions.milkledger.presentation.customer.sales

import com.miassolutions.milkledger.data.local.relations.SaleWithCustomer
import com.miassolutions.milkledger.domain.model.Sale

data class SaleUi(
    val data: Sale,
    val accumulatedBalance: Double
)

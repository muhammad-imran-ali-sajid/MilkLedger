package com.miassolutions.milkledger.presentation.customer.sales.model

import com.miassolutions.milkledger.domain.model.Sale

data class SaleUi(
    val data: Sale,
    val accumulatedBalance: Double
)
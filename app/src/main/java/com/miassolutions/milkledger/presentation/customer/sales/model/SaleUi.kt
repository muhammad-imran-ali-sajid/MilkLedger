package com.miassolutions.milkledger.presentation.customer.sales.model

import com.miassolutions.milkledger.data.local.relations.SaleWithCustomer
import com.miassolutions.milkledger.domain.model.Sale
import com.miassolutions.milkledger.domain.model.SaleWithCustomerModel

data class SaleUi(
    val data: Sale,
    val accumulatedBalance: Double
)

data class SaleWithCustomerUI(
    val data: SaleWithCustomerModel,
    val accumulatedBalance: Double
)
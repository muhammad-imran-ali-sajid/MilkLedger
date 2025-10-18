package com.miassolutions.milkledger.presentation.details

import com.miassolutions.milkledger.data.local.relations.SaleWithCustomer

data class CustomerDetailUiState(
    val selectedCustomerId : String? = null,
    val customerName :String = "",
    val customerDetailList : List<CustomerDetailModel> = emptyList()

)

package com.miassolutions.milkledger.presentation.details

data class CustomerDetailUiState(
    val selectedCustomerId : String? = null,
    val customerName :String = "",
    val customerDetailList : List<CustomerDetailModel> = emptyList()

)

package com.miassolutions.milkledger.presentation.customerandsales.sales.saleform.state

import com.miassolutions.milkledger.presentation.customerandsales.customer.model.DropDownCustomerListUi
import java.time.LocalDate

data class SaleFormUiState(
    val mode: SaleMode = SaleMode.ADD,
    val saleDate: LocalDate = LocalDate.now(),

    val customers: List<DropDownCustomerListUi> = emptyList(),
    val selectedCustomer: DropDownCustomerListUi? = null,

    val volume: String = "",
    val deduction: String = "",

    val netMilk: Double = 0.0,

    val rateUsed: Double = 0.0,
    val price: Double = 0.0,

    val receivedAmount: String = "",
    val receivedDate: LocalDate = LocalDate.now(),

    val balance: Double = 0.0,

    val notes: String = "",

    val isSaving: Boolean = false,
    val error: String? = null

)



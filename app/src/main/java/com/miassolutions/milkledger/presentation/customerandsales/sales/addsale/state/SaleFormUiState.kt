package com.miassolutions.milkledger.presentation.customerandsales.sales.addsale.state

import com.miassolutions.milkledger.presentation.customerandsales.customer.model.CustomerUi
import java.time.LocalDate

data class SaleFormUiState(
    val mode: SaleMode = SaleMode.ADD,
    val saleDate: LocalDate = LocalDate.now(),

    val customers: List<CustomerUi> = emptyList(),
    val selectedCustomer: CustomerUi? = null,

    val volume: String = "",
    val deduction: String = "",

    val netMilk: Double = 0.0,

    val rate: Double = 0.0,
    val price: Double = 0.0,

    val receivedAmount: String = "",
    val receivedDate: LocalDate? = null,

    val balance: Double = 0.0,

    val notes: String = "",

    val isSaving: Boolean = false,
    val error: String? = null

)



package com.miassolutions.milkledger.presentation.sales

import com.miassolutions.milkledger.data.local.relations.SaleWithCustomer
import java.time.LocalDate

data class SalesUiState(
    val currentDate: LocalDate = LocalDate.now(),
    val salesForDate : List<SaleWithCustomer> = emptyList(),
    val grandSaleTotalForDate : Double = 0.0,
    val selectedCustomerId : String? = null,
    val navToLedgerForCustomerId : String? = null,
    val totalMilk : Double = 0.0,
    val totalDeduction : Double = 0.0,
    val totalAmount : Double = 0.0,
    val avgRatePerLiter : Double = 0.0
)


sealed class  SalesUiEvent {
    data class OnCustomerSelected(val supplierId: String) : SalesUiEvent()
}
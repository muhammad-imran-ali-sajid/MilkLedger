package com.miassolutions.milkledger.presentation.customerandsales.sales.addsale.mapper

import com.miassolutions.milkledger.domain.model.Sale
import com.miassolutions.milkledger.presentation.customerandsales.sales.addsale.state.SaleFormUiState
import java.util.UUID

fun SaleFormUiState.toDomain(): Sale =
    Sale(
        id = UUID.randomUUID().toString(),
        customerId = selectedCustomer!!.id,
        date = saleDate,
        volume = volume.toDoubleOrNull() ?: 0.0,
        deduction = deduction.toDoubleOrNull() ?: 0.0,
        netMilk = netMilk,
        rateUsed = rate,
        price = price,
        paid = receivedAmount.toDoubleOrNull() ?: 0.0,
        notes = notes.orEmpty(),
        paidAt = receivedDate,
        balance = balance,
    )

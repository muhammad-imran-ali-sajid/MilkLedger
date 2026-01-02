package com.miassolutions.milkledger.features.sale.mapper

import com.miassolutions.milkledger.features.sale.data.local.SaleEntity
import com.miassolutions.milkledger.features.sale.domain.model.Sale
import com.miassolutions.milkledger.features.sale.ui.saleform.SaleFormUiState
import com.miassolutions.milkledger.utils.extensions.toLocalDate
import com.miassolutions.milkledger.utils.extensions.toMillis
import java.util.UUID


fun SaleEntity.toDomain(): Sale =
    Sale(
        id = saleId,
        customerId = customerId,
        date = dateMillis.toLocalDate(),
        paidAt = paidAtMillis?.toLocalDate(),
        volume = volume,
        deduction = deduction,
        netMilk = netMilk,
        price = totalAmount,
        paid = paid,
        balance = balance,
        rateUsed = rateUsed,
        notes = notes
    )

fun Sale.toEntity(): SaleEntity =
    SaleEntity(
        saleId = id,
        customerId = customerId,
        dateMillis = date.toMillis(),
        paidAtMillis = paidAt?.toMillis(),
        volume = volume,
        deduction = deduction,
        netMilk = netMilk,
        totalAmount = price,
        paid = paid,
        balance = balance,
        rateUsed = rateUsed,
        notes = notes
    )

fun SaleFormUiState.toDomain(): Sale =
    Sale(
        id = this.saleId ?: UUID.randomUUID().toString(),
        customerId = selectedCustomer!!.id,
        date = saleDate,
        volume = volume.toDoubleOrNull() ?: 0.0,
        deduction = deduction.toDoubleOrNull() ?: 0.0,
        netMilk = netMilk,
        rateUsed = rateUsed,
        price = price,
        paid = receivedAmount.toDoubleOrNull() ?: 0.0,
        notes = notes.orEmpty(),
        paidAt = receivedDate,
        balance = balance,
    )

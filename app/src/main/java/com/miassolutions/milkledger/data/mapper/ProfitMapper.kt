package com.miassolutions.milkledger.data.mapper

import com.miassolutions.milkledger.core.extensions.toLocalDate
import com.miassolutions.milkledger.core.extensions.toMillis
import com.miassolutions.milkledger.data.local.entities.ProfitEntity
import com.miassolutions.milkledger.domain.model.Profit

fun ProfitEntity.toDomain(): Profit =
    Profit(
        date = dateMillis.toLocalDate(),
        grossProfit = grossProfit,
        netProfit = netProfit,
        receivedProfit = receivedProfit,
        notes = notes
    )

fun Profit.toEntity(): ProfitEntity =
    ProfitEntity(
        dateMillis = date.toMillis(),
        grossProfit = grossProfit,
        netProfit = netProfit,
        receivedProfit = receivedProfit,
        notes = notes
    )

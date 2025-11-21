package com.miassolutions.milkledger.data.mapper

import com.miassolutions.milkledger.data.local.entities.ProfitEntity
import com.miassolutions.milkledger.domain.model.Profit


fun ProfitEntity.toProfit() = with(this) {
    Profit(
        profitId = profitId,
        receivedDate = receivedDate,
        netProfit = netProfit,
        receivedProfit = receivedProfit,
        notes = notes,
        isSynced = isSynced,
        updatedAt = updatedAt,
        deletedAt = deletedAt
    )
}


fun Profit.toProfitEntity() = with(this) {
    ProfitEntity(
        profitId = profitId,
        receivedDate = receivedDate,
        netProfit = netProfit,
        receivedProfit = receivedProfit,
        notes = notes,
        isSynced = isSynced,
        updatedAt = updatedAt,
        deletedAt = deletedAt
    )
}
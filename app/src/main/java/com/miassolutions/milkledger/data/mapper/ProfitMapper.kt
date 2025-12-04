package com.miassolutions.milkledger.data.mapper

import com.miassolutions.milkledger.data.local.entities.ProfitEntity
import com.miassolutions.milkledger.data.remote.model.FirestoreProfit
import com.miassolutions.milkledger.domain.model.Profit
import com.miassolutions.milkledger.presentation.profit.ProfitListModel
import java.time.LocalDate


fun ProfitEntity.toProfit() = with(this) {
    Profit(
        profitId = profitId,
        receivedDate = receivedDate,

        receivedProfit = receivedProfit,
        notes = notes,
        isSynced = isSynced,
        updatedAt = updatedAt,
        deletedAt = deletedAt,
        netProfit = netProfit
    )
}

fun ProfitEntity.toProfitList(profitAfterPersonalExpenses: Double?) = with(this) {
    ProfitListModel(
        id = profitId,
        date = receivedDate,
        profit = netProfit,
        profitReceived = receivedProfit,
        balance = netProfit - receivedProfit,
        profitAfterPersonalExpenses = profitAfterPersonalExpenses ?:0.0
    )
}




fun ProfitListModel.toEntity() = with(this) {
    ProfitEntity(

        receivedDate = date,
        netProfit = profit,
        receivedProfit = profitReceived,
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

fun ProfitEntity.toFirestore() = with(this) {
    FirestoreProfit(
        profitId = profitId,
        receivedDate = receivedDate.toString(),
        netProfit = netProfit,
        receivedProfit = receivedProfit,
        notes = notes,
        isSynced = isSynced,
        updatedAt = updatedAt,
        deletedAt = deletedAt
    )
}

fun FirestoreProfit.toEntity() = with(this) {
    ProfitEntity(
        profitId = profitId,
        receivedDate = LocalDate.parse(receivedDate),
        netProfit = netProfit,
        receivedProfit = receivedProfit,
        notes = notes,
        isSynced = isSynced,
        updatedAt = updatedAt,
        deletedAt = deletedAt
    )
}
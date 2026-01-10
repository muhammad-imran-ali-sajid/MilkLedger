package com.miassolutions.milkledger.features.owner.domain

data class DailyProfitTuple(
    val dateMillis: Long,
    val dailyTotal: Long
)
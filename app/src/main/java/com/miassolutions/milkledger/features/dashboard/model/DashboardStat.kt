package com.miassolutions.milkledger.features.dashboard.model

import androidx.annotation.ColorRes

data class DashboardStat(
    val title: String,
    val value: String,
    @ColorRes val valueColor: Int? = null // Agar color change karna ho (Red/Green)
)
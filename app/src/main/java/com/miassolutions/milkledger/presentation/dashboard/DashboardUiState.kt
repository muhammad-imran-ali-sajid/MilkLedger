package com.miassolutions.milkledger.presentation.dashboard

import java.time.LocalDate

data class DashboardUiState(
    val date: LocalDate = LocalDate.now(),
    val totalSales: Double = 0.0,
    val totalPurchases: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val profit: Double = 0.0
)
package com.miassolutions.milkledger.features.dashboard.data

import com.miassolutions.milkledger.features.dashboard.DashboardUiState
import com.miassolutions.milkledger.utils.extensions.toRupees
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class DashboardRepository @Inject constructor(
    private val dashboardDao: DashboardDao
) {

    fun getDashboardData(start: Long, end: Long): Flow<DashboardUiState> {
        return combine(
            dashboardDao.getPurchaseStats(start, end),
            dashboardDao.getSaleStats(start, end),
            dashboardDao.getBusinessExpenseTotal(start, end)
        ) { purchase, sale, expenseTotal ->

            // 1. Calculations
            val avgPP = if (purchase.totalVolume > 0)
                (purchase.totalAmount.toRupees() / purchase.totalVolume) else 0.0

            val avgSP = if (sale.totalVolume > 0)
                (sale.totalAmount.toRupees() / sale.totalVolume) else 0.0

            val grossProfit = sale.totalAmount - (purchase.totalAmount + expenseTotal)

            // 2. Map to UI State
            DashboardUiState(
                isLoading = false,

                // Money
                totalPurchases = purchase.totalAmount,
                totalSales = sale.totalAmount,
                totalExpenses = expenseTotal,
                grossProfit = grossProfit,

                // Quantities
                milkPurchasedQty = purchase.totalVolume,
                milkSoldQty = sale.totalVolume,
                qtyDiff = sale.totalVolume - purchase.totalVolume, // Loss/Gain

                // Averages
                avgPurchasePrice = avgPP,
                avgSalePrice = avgSP,
                avgPriceDiff = avgSP - avgPP,

                // Quality
                avgFat = purchase.avgFat,
                avgLr = purchase.avgLr,
                totalTs = purchase.avgTs // Ya total TS agar sum krna ho
            )
        }
    }
}
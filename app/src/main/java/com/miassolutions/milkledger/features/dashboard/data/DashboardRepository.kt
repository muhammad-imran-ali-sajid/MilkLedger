package com.miassolutions.milkledger.features.dashboard.data

import com.miassolutions.milkledger.core.contstants.Constants
import com.miassolutions.milkledger.core.localdb.ledger.LedgerDao
import com.miassolutions.milkledger.features.dashboard.DashboardUiState
import com.miassolutions.milkledger.utils.extensions.toRupees
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class DashboardRepository @Inject constructor(
    private val dashboardDao: DashboardDao,
    private val ledgerDao: LedgerDao
) {

    fun getDashboardData(start: Long, end: Long): Flow<DashboardUiState> {
        return combine(
            dashboardDao.getPurchaseStats(start, end),
            dashboardDao.getSaleStats(start, end),
            dashboardDao.getBusinessExpenseTotal(start, end),
            ledgerDao.getPersonalExpenseSum(start, end, Constants.PREFIX_EXPENSE)
        ) { purchase, sale, expenseTotal,personalExp ->

            // 1. Calculations
            val avgPP = if (purchase.totalVolume > 0)
                (purchase.totalAmount.toRupees() / purchase.totalVolume)
            else 0.0

            val avgSP = if (sale.totalVolume > 0)
                (sale.totalAmount.toRupees() / purchase.totalVolume)
            else 0.0


            val grossProfit = sale.totalAmount - (purchase.totalAmount + expenseTotal)
            
            // 🔥 NAYA: Remaining Balance (Pocket Cash)
            val calculatedRemaining = grossProfit - personalExp

            // 2. Map to UI State
            DashboardUiState(
                isLoading = false,

                // Money
                totalPurchases = purchase.totalAmount,
                totalSales = sale.totalAmount,
                totalExpenses = expenseTotal,
                grossProfit = grossProfit,
                totalPersonalExpense = personalExp,
                remainingBalance = calculatedRemaining,
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
                qualityVolume = purchase.qualityVolume,
                totalTs = purchase.totalTs // Ya total TS agar sum krna ho
            )
        }
    }
}
package com.miassolutions.milkledger.presentation.customerandsales.sales.saleslist.usecase

import com.miassolutions.milkledger.data.repository.CustomerRepository
import com.miassolutions.milkledger.data.repository.PurchaseRepository
import com.miassolutions.milkledger.data.repository.SalesRepository
import com.miassolutions.milkledger.domain.model.SaleProjection
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate
import javax.inject.Inject

class ObserveSalesForDateUseCase @Inject constructor(
    private val salesRepository: SalesRepository,
    private val purchaseRepository: PurchaseRepository,
    private val customerRepository: CustomerRepository
) {

    operator fun invoke(date: LocalDate): Flow<SalesDaySummary> =
        combine(
            salesRepository.getSalesByDate(date),
            purchaseRepository.getPurchasesByDate(date),
            customerRepository.getAllCustomers()
        ) { sales, purchases, customers ->

            val customerMap = customers.associateBy { it.id }

            // ---- Totals ----
            val totalMilk = sales.sumOf { it.volume }
            val totalDeduction = sales.sumOf { it.deduction }
            val totalNet = sales.sumOf { it.netMilk }

            val totalAmount = sales.sumOf { it.price }
            val received = sales.sumOf { it.paid }
            val totalBalance = sales.sumOf { it.balance }

            val purchaseMilk = purchases.sumOf { it.milkAmount }
            val avgRate =
                if (purchaseMilk > 0) totalAmount / purchaseMilk else 0.0

            // ---- Accumulated balance (ledger-safe) ----
            val balanceCache = mutableMapOf<String, Double>()

            val projections = sales
                .sortedBy { it.date }
                .map { sale ->

                    val customerName =
                        customerMap[sale.customerId]?.name ?: "Unknown"

                    val runningBalance =
                        balanceCache.getOrDefault(sale.customerId, 0.0) +
                                sale.balance

                    balanceCache[sale.customerId] = runningBalance

                    SaleProjection(
                        sale = sale,
                        customerName = customerName,
                        accumulatedBalance = runningBalance
                    )
                }

            SalesDaySummary(
                sales = projections,
                totalMilk = totalMilk,
                totalDeduction = totalDeduction,
                totalNetMilk = totalNet,
                totalAmount = totalAmount,
                receivedAmount = received,
                totalBalance = totalBalance,
                avgRate = avgRate
            )
        }
}



data class SalesDaySummary(
    val sales: List<SaleProjection>,
    val totalMilk: Double,
    val totalDeduction: Double,
    val totalNetMilk: Double,
    val totalAmount: Double,
    val receivedAmount: Double,
    val totalBalance: Double,
    val avgRate: Double
)


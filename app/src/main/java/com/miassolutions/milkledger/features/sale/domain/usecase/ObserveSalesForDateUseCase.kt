package com.miassolutions.milkledger.features.sale.domain.usecase

import com.miassolutions.milkledger.features.customer.data.repository.CustomerRepository
import com.miassolutions.milkledger.features.purchase.data.PurchaseRepository
import com.miassolutions.milkledger.features.sale.data.repository.SaleRepository
import com.miassolutions.milkledger.features.sale.domain.model.SaleProjection
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate
import javax.inject.Inject

class ObserveSalesForDateUseCase @Inject constructor(
    private val salesRepository: SaleRepository,
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
            val totalNetMilk = sales.sumOf { it.netMilk }

            val totalAmount = sales.sumOf { it.price }
            val received = sales.sumOf { it.paid }
            val totalBalance = sales.sumOf { it.balance }

            val purchaseMilk = purchases.sumOf { it.milkAmount }
            val avgRate =
                if (totalNetMilk > 0) totalAmount / totalNetMilk else 0.0 // here purchase milk will be used todo()

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
                totalNetMilk = totalNetMilk,
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


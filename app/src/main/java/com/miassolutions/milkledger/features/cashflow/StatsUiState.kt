package com.miassolutions.milkledger.features.cashflow

import java.time.LocalDate

data class StatDashboardState(
    val targetDate: LocalDate = LocalDate.now(),
    val customerPayments: List<CustomerPaidSummary> = emptyList(),
    val supplierPayments: List<SupplierPaidSummary> = emptyList(),
    val businessExpenseList: List<BusinessExpenseSummary> = emptyList(),
    val personalExpenseList: List<PersonalExpenseSummary> = emptyList(),
    val totalCustomerPayment: Double = 0.0,
    val totalSupplierPayment: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val isLoading: Boolean = true
)

// Data classes for the summaries (re-used)
data class CustomerPaidSummary(val customerName: String, val paidAmount: Double, val volume: Double?)
data class SupplierPaidSummary(val supplierName: String, val paidAmount: Double, val volume: Double?)

data class BusinessExpenseSummary(
    val expenseTitle: String,
    val expenseAmount: Double,
)

data class PersonalExpenseSummary(
    val expenseTitle: String,
    val expenseAmount: Double,
)

data class ProfitSummary(
    val date: String,
    val profitAmount: Double,
)

data class SumSummary(
    val title: String,
    val sum: Double
)


sealed class StatListItem {
    // 1. For Section Headers (e.g., "Customer Payments")
    data class Header(val title: String, val volume: String?=null, val amount : String) : StatListItem()
    data class Empty(val message: String) : StatListItem()
    data class MilkTotalSummary(val label: String, val volume: Double, val amount: Double) : StatListItem()
    data class TotalSummary(val label: String, val amount: Double) : StatListItem()
    data class CustomerItem(val summary: CustomerPaidSummary) : StatListItem()
    data class SupplierItem(val summary: SupplierPaidSummary) : StatListItem()
    data class BusinessExpenseItem(val summary: BusinessExpenseSummary) : StatListItem()
    data class PersonalExpenseItem(val summary: PersonalExpenseSummary) : StatListItem()
    data class ProfitItem(val summary: ProfitSummary) : StatListItem()
}
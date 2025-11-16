package com.miassolutions.milkledger.presentation.stats

import java.time.LocalDate

data class StatDashboardState(
    val targetDate: LocalDate = LocalDate.now(),
    val customerPayments: List<CustomerPaidSummary> = emptyList(),
    val supplierPayments: List<SupplierPaidSummary> = emptyList(),
    val isLoading: Boolean = true
)

// Data classes for the summaries (re-used)
data class CustomerPaidSummary(val customerName: String, val paidAmount: Double)
data class SupplierPaidSummary(val supplierName: String, val paidAmount: Double)



sealed class StatListItem {
    // 1. For Section Headers (e.g., "Customer Payments")
    data class Header(val title: String) : StatListItem()

    // 2. For Customer Data
    data class CustomerItem(val summary: CustomerPaidSummary) : StatListItem()

    // 3. For Supplier Data
    data class SupplierItem(val summary: SupplierPaidSummary) : StatListItem()
}
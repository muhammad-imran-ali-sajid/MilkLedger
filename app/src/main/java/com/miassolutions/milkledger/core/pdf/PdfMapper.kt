package com.miassolutions.milkledger.core.pdf

import com.miassolutions.milkledger.features.purchase.model.MilkPurchaseUiModel
import com.miassolutions.milkledger.features.purchase.model.PurchaseSummary
import com.miassolutions.milkledger.utils.extensions.toLocalDate
import com.miassolutions.milkledger.utils.extensions.toPrice

object PdfMapper {

    fun mapSupplierHistoryToPdf(
        supplierName: String,
        dateRange: String,
        list: List<MilkPurchaseUiModel>,
        stats: PurchaseSummary
    ): PdfReportModel {

        // 1. Columns Setup
        val headers = listOf("Date", "Vol", "Fat", "Rate", "Total", "Paid")
        val weights = floatArrayOf(2f, 1f, 1f, 1.5f, 2f, 2f) // Date thori chori, baki normal

        // 2. Rows Conversion
        val rows = list.map { item ->
            listOf(
                item.dateMillis.toLocalDate().toString(), // Date
                String.format("%.1f", item.volume),       // Volume
                String.format("%.1f", item.fat),          // Fat
                item.rate.toInt().toString(),             // Rate
                item.totalAmount.toPrice(),               // Total
                item.paymentMade.toPrice()                // Paid
            )
        }

        // 3. Summary
        val summary = listOf(
            "Total Milk" to "${stats.totalVolume} L",
            "Total Amount" to stats.totalAmount.toPrice(),
            "Total Paid" to stats.totalPaid.toPrice()
        )

        return PdfReportModel(
            fileName = "Report_$supplierName.pdf",
            shopName = "My Milk Shop",
            reportTitle = "Ledger: $supplierName",
            dateRange = dateRange,
            columnHeaders = headers,
            columnWeights = weights,
            rows = rows,
            summaryLabels = summary
        )
    }
}
package com.miassolutions.milkledger.core.pdf

import com.miassolutions.milkledger.features.purchase.model.MilkPurchaseUiModel
import com.miassolutions.milkledger.utils.extensions.toLocalDate
import com.miassolutions.milkledger.utils.extensions.toPrice

object PdfMapper {


    fun mapSupplierHistoryToPdf(
        supplierName: String,
        dateRange: String,
        list: List<MilkPurchaseUiModel>,
        initialBalance: Long = 0 // Agar pichla balance bhi lana ho
    ): PdfReportModel {

        // 1. Columns Setup (Balance Added)
        val headers = listOf("Date", "Vol", "Fat", "LR", "Rate", "Total", "Paid", "Bal")
        // Weights adjust kiye taake Balance fit ho sake
        val weights = floatArrayOf(2f, 1.2f, 0.8f, 0.8f, 1f, 1.5f, 1.5f, 1.8f)

        // Variables for Calculation
        var runningBalance = initialBalance

        // Accumulators for Summary
        var sumVol = 0.0
        var sumAmount = 0L
        var sumPaid = 0L

        // Weighted Average Accumulators
        var sumVolFat = 0.0 // Vol * Fat
        var sumVolLr = 0.0  // Vol * LR
        var sumVolTs = 0.0  // Vol * TS
        var validVolFat = 0.0 // Vol where Fat > 0

        // 2. Process Rows
        // List ko reverse karein agar purani date upar dikhani hai (Ledger style)
        // Usually Ledger: Oldest -> Newest
        val sortedList = list.sortedBy { it.dateMillis }

        val rows = sortedList.map { item ->

            // A. Update Running Balance (Purchase Logic)
            // Balance barhta hai TotalAmount se, kam hota hai Paid se
            val netChange = item.totalAmount - item.paymentMade
            runningBalance += netChange

            // B. Accumulate Stats for Summary
            sumVol += item.volume
            sumAmount += item.totalAmount
            sumPaid += item.paymentMade

            if (item.fat > 0) {
                sumVolFat += (item.volume * item.fat)
                sumVolLr += (item.volume * item.lr)
                sumVolTs += (item.volume * item.ts)
                validVolFat += item.volume
            }

            // C. Create Row Data
            listOf(
                item.dateMillis.toLocalDate().toString(),
                String.format("%.1f", item.volume),
                String.format("%.1f", item.fat),
                String.format("%.1f", item.lr),
                item.rate.toInt().toString(),
                item.totalAmount.toPrice(),
                item.paymentMade.toPrice(),
                runningBalance.toPrice() // 🔥 Date wise Balance
            )
        }

        // 3. Calculate Averages
        val avgFat = if (validVolFat > 0) sumVolFat / validVolFat else 0.0
        val avgLr = if (validVolFat > 0) sumVolLr / validVolFat else 0.0
        val avgTs = if (validVolFat > 0) sumVolTs / validVolFat else 0.0

        // Average Rate (Weighted)
        val avgRate = if (sumVol > 0) (sumAmount.toDouble() / sumVol) else 0.0

        // 4. Create Summary Row
        val summaryRow = listOf(
            "TOTAL",                    // Date Column me "TOTAL" likha ayega
            String.format("%.1f", sumVol),
            String.format("%.1f", avgFat),
            String.format("%.1f", avgLr),
            avgRate.toInt().toString(), // Avg Rate
            sumAmount.toPrice(),
            sumPaid.toPrice(),
            runningBalance.toPrice()    // Closing Balance
        )

        return PdfReportModel(
            fileName = "Ledger_${supplierName}.pdf",
            shopName = "Bismillah Milk Shop", // Apni shop ka naam dynamic kar len
            reportTitle = "Supplier Ledger: $supplierName",
            dateRange = dateRange,
            columnHeaders = headers,
            columnWeights = weights,
            rows = rows,
            summaryRow = summaryRow // 🔥 Pass Summary Row
        )
    }
}

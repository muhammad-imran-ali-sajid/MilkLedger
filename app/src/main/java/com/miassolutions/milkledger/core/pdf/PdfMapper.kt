package com.miassolutions.milkledger.core.pdf

import com.miassolutions.milkledger.features.purchase.model.MilkPurchaseUiModel
import com.miassolutions.milkledger.features.sale.model.MilkSaleUiModel
import com.miassolutions.milkledger.utils.extensions.format
import com.miassolutions.milkledger.utils.extensions.formatSignedBalance
import com.miassolutions.milkledger.utils.extensions.toCompleteDateFormat
import com.miassolutions.milkledger.utils.extensions.toFormattedMilk
import com.miassolutions.milkledger.utils.extensions.toLocalDate
import com.miassolutions.milkledger.utils.extensions.toPrice
import com.miassolutions.milkledger.utils.extensions.toSignedBalance

object PdfMapper {

    fun mapSupplierHistoryToPdf(
        supplierName: String,
        dateRange: String,
        list: List<MilkPurchaseUiModel>,
        initialBalance: Long = 0
    ): PdfReportModel {

        // 1. Columns Setup (Added "TS" column)
        val headers = listOf("Date", "Vol", "Fat", "LR", "TS", "Rate", "Price", "Paid", "Bal")

        // Weights adjust kiye taake TS aur Bal fit ho saken
        // Total sum ~ 10-11 range me rakha hai taake page par fit ho
        val weights = floatArrayOf(1.2f, 1.1f, 0.8f, 0.8f, 1.2f, 1f, 1.2f, 1.2f, 1.2f)

        // Variables for Calculation
        var runningBalance = initialBalance

        // Accumulators for Totals
        var sumVol = 0.0
        var sumAmount = 0L
        var sumPaid = 0L

        // Accumulators for Simple Averages (Sum of values)
        var sumFat = 0.0
        var sumLr = 0.0
        var sumTs = 0.0
        var sumRate = 0.0

        // Counters (Kin entries me data majood tha)
        var countFat = 0
        var countLr = 0
        var countTs = 0
        var countRate = 0

        // 2. Process Rows (Sorted Oldest -> Newest for correct Ledger balance)
        val sortedList = list.sortedBy { it.dateMillis }

        val rows = sortedList.map { item ->

            // A. Balance Logic (User Requirement: Pay More = Positive)
            // Balance = Payment (Credit) - Purchase Amount (Debit)
            val netChange = item.paymentMade - item.totalAmount
            runningBalance += netChange

            // B. Accumulate for Summary (Simple Sums)
            sumVol += item.volume
            sumAmount += item.totalAmount
            sumPaid += item.paymentMade

            // Logic for Simple Averages (Zero values exclude krne k liye)
            if (item.fat > 0) {
                sumFat += item.fat
                countFat++
            }
            if (item.lr > 0) {
                sumLr += item.lr
                countLr++
            }
            if (item.ts > 0) {
                sumTs += item.ts
                countTs++
            }
            // Rate logic: Agar rate 0 se bara hai to count karo
            if (item.rate > 0) {
                sumRate += item.rate
                countRate++
            }

            // C. Create Row Data
            listOf(
                item.dateMillis.toLocalDate().toCompleteDateFormat(),
                String.format("%.1f", item.volume),
                String.format("%.1f", item.fat),
                String.format("%.1f", item.lr),
                String.format("%.2f", item.ts), // 🔥 New TS Column
                item.rate.toInt().toString(),
                item.totalAmount.toPrice(),
                item.paymentMade.toPrice(),

                // Balance with Explicit Sign (+/-)
                runningBalance.toSignedBalance()
            )
        }

        // 3. Calculate Simple Averages (Flat Average)
        val avgFat = if (countFat > 0) sumFat / countFat else 0.0
        val avgLr = if (countLr > 0) sumLr / countLr else 0.0
        val avgTs = if (countTs > 0) sumTs / countTs else 0.0
        val avgRate = if (countRate > 0) sumRate / countRate else 0.0

        // 4. Create Summary Row
        val summaryRow = listOf(
            "TOTAL",
            String.format("%.1f", sumVol), // Total Volume
            String.format("%.2f", avgFat), // Simple Avg Fat
            String.format("%.2f", avgLr),  // Simple Avg LR
            String.format("%.2f", avgTs),  // Simple Avg TS
            avgRate.toInt().toString(),    // Simple Avg Rate
            sumAmount.toPrice(),           // Total Purchase Amount
            sumPaid.toPrice(),             // Total Paid
            formatBalance(runningBalance)  // Closing Balance
        )

        return PdfReportModel(
            fileName = "Ledger_${supplierName}.pdf",
            shopName = "GMC Milk Collection",
            reportTitle = "Supplier Ledger: $supplierName",
            dateRange = dateRange,
            columnHeaders = headers,
            columnWeights = weights,
            rows = rows,
            summaryRow = summaryRow
        )
    }



    fun mapCustomerHistoryToPdf(
        customerName: String,
        dateRang: String,
        list: List<MilkSaleUiModel>,
        initialBalance: Long = 0
    ): PdfReportModel {
        val headers = listOf("Date", "Vol", "Ded.", "Net", "Rate", "Price", "Rec.", "Bal")
        //total sum 10~11 me ho ta ke page pr fit aa skay
        val weights = floatArrayOf(1.0f, 0.9f, 0.9f, 0.9f,0.9f, 1.2f, 1.2f, 1.2f)

        var runningBalance = initialBalance


        //accumulators for totals
        var sumVol = 0.0
        var sumDeduction = 0.0
        var sumNetVol = 0.0
        var sumAmount = 0.0
        var sumReceived = 0.0


        //accumulator for avg
        var sumRate = 0.0

        // counter for avg
        var countRate = 0

        // process rows -> sorted oldest to newest
        val sortedList = list.sortedBy { it.dateMillis }

        val rows = sortedList.map { item ->
            // balance logic
            val netChange = item.paymentReceived - item.totalAmount
            runningBalance += netChange

            // accumulate for summary
            sumVol += item.quantity
            sumDeduction += item.deduction
            sumNetVol += item.netQuantity
            sumAmount += item.totalAmount
            sumReceived += item.paymentReceived


            if (item.rate > 0) {
                sumRate += item.rate
                countRate++
            }

            listOf(
                item.dateMillis.toLocalDate().toCompleteDateFormat(),
                item.quantity.toFormattedMilk(),
                item.deduction.toFormattedMilk(),
                item.netQuantity.toFormattedMilk(),
                item.rate.format(1),
                item.totalAmount.toPrice(),
                item.paymentReceived.toPrice(),
                runningBalance.formatSignedBalance()
            )

        }

        val avgRate = if (countRate > 0) sumRate / countRate else 0.0

        val summaryRow = listOf(
            "TOTAL",
            sumVol.toFormattedMilk(),
            sumDeduction.toFormattedMilk(),
            sumNetVol.toFormattedMilk(),
            avgRate.format(1),
            sumAmount.toPrice(),
            sumReceived.toPrice(),
            runningBalance.formatSignedBalance()

        )


        return PdfReportModel(
            fileName = "Ledger_${customerName}.pdf",
            shopName = "GMC Milk Collection",
            reportTitle = "Customer Ledger: $customerName",
            dateRange = dateRang,
            columnHeaders = headers,
            columnWeights = weights,
            rows = rows,
            summaryRow = summaryRow,

            )

    }

    // Helper to format balance with +/- sign
    private fun formatBalance(balance: Long): String {
        return when {
            balance > 0 -> "+${balance.toPrice()}" // Advance (Positive)
            balance < 0 -> balance.toPrice()       // Due (Negative, toPrice usually handles minus or brackets)
            else -> "0"
        }
    }
}
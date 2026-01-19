package com.miassolutions.milkledger.core.pdf

import com.miassolutions.milkledger.features.dashboard.DashboardUiState
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
                item.volume.toFormattedMilk(),
                item.fat.toFormattedMilk(),
                item.lr.toFormattedMilk(),
                item.ts.toFormattedMilk(),
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
            sumVol.toFormattedMilk(),
            avgFat.toFormattedMilk(),
            avgLr.toFormattedMilk(),
            avgTs.toFormattedMilk(),
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


    /**
     * Maps Dashboard State to PDF Model.
     * Structures data as a 2-column table: [Metric Name] | [Value]
     */
    fun mapDashboardToPdf(
        state: DashboardUiState,
        dateRange: String
    ): PdfReportModel {

        // 1. Column Setup
        val headers = listOf("Description", "Value")
        // Weight: 70% width for Description, 30% for Value
        val weights = floatArrayOf(2.0f, 1.0f)

        // 2. Build Rows (Categorized for readability)
        val rows = mutableListOf<List<String>>()

        // --- SECTION: FINANCIALS ---
        rows.add(listOf("Total Sales (Revenue)", state.totalSales.toPrice()))
        rows.add(listOf("Total Purchases (Cost)", state.totalPurchases.toPrice()))
        rows.add(listOf("Total Expenses", state.totalExpenses.toPrice()))
        rows.add(listOf("GROSS PROFIT", state.grossProfit.toPrice()))

        // Spacer Row (Empty strings to create visual gap if needed, or just list sequentially)
        // Note: If you want clear sections, you might prefix names like "MILK - Purchased"

        // --- SECTION: MILK QUANTITY ---
        rows.add(listOf("Milk Purchased", "${state.milkPurchasedQty.format(1)} L"))
        rows.add(listOf("Milk Sold", "${state.milkSoldQty.format(1)} L"))
        rows.add(listOf("Quantity Difference", "${state.qtyDiff.format(1)} L"))

        // --- SECTION: PRICING ---
        rows.add(listOf("Avg. Purchase Price", state.avgPurchasePrice.format(1)))
        rows.add(listOf("Avg. Sale Price", state.avgSalePrice.format(1)))
        rows.add(listOf("Price Margin", state.avgPriceDiff.format(2)))

        // --- SECTION: QUALITY (Avg) ---
        // Using qualityVolume to show what volume these averages are based on
        val volStr = "(${state.qualityVolume.format(0)} L)"
        rows.add(listOf("Avg Fat $volStr", state.avgFat.format(2)))
        rows.add(listOf("Avg LR $volStr", state.avgLr.format(2)))
        rows.add(listOf("Total TS $volStr", state.totalTs.format(2)))

        // 3. Bottom Summary (Highlighting key Financial Result)
        // Since the table rows are distinct metrics (not a sum-able list),
        // we use the bottom summary to re-emphasize the Net Profit.
        val summaryLabels = listOf(
            "NET PROFIT" to state.grossProfit.toPrice()
        )

        return PdfReportModel(
            fileName = "Dashboard_Report.pdf",
            shopName = "GMC Milk Collection", // Or inject this from a UserPreference
            reportTitle = "Business Overview",
            dateRange = dateRange,
            columnHeaders = headers,
            columnWeights = weights,
            rows = rows,
            summaryRow = null, // No footer row needed for the table itself
            summaryLabels = summaryLabels
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
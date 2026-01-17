package com.miassolutions.milkledger.core.pdf

import com.miassolutions.milkledger.features.sale.model.MilkSaleUiModel
import com.miassolutions.milkledger.utils.extensions.format
import com.miassolutions.milkledger.utils.extensions.formatSignedBalance
import com.miassolutions.milkledger.utils.extensions.toCompleteDateFormat
import com.miassolutions.milkledger.utils.extensions.toFormattedMilk
import com.miassolutions.milkledger.utils.extensions.toLocalDate
import com.miassolutions.milkledger.utils.extensions.toPrice

object PdfCustomerMapper {

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
}
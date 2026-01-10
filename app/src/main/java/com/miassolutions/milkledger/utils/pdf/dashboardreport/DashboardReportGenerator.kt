package com.miassolutions.milkledger.utils.pdf.dashboardreport

import android.content.Context
import android.graphics.pdf.PdfDocument
import android.view.LayoutInflater
import android.view.View
import com.miassolutions.milkledger.databinding.LayoutDashboardPdfBinding

import com.miassolutions.milkledger.utils.pdf.PdfUtils
import java.io.File
import java.io.FileOutputStream

object DashboardReportGenerator {

//    /**
//     * Generates the PDF document from the layout, saves it to a file, and returns the file.
//     * The file naming uses the improved logic from PdfUtils (BaseName_YYYYMMDD_HHMMSS.pdf).
//     */
//    private fun generateDashboardPdf(
//        context: Context,
//        data: DashboardSummaryPdf,
//        baseName: String,
//        showLogo: Boolean = false,
//        logoResId: Int? = null
//    ): File {
//        val inflater = LayoutInflater.from(context)
//        val binding = LayoutDashboardPdfBinding.inflate(inflater)
//
//
//        val file = PdfUtils.getPdfFile(context, baseName)
//        val receiptName = file.name.removeSuffix(".pdf")
//        // Header info
//        with(binding) {
//            tvDateRange.text = data.dateRange
//            tvTotalPurchasePrice.text = data.totalPurchasePrice
//            tvTotalSalePrice.text = data.totalSalePrice
//            tvBusinessExpenses.text = data.businessExpenses
//            tvProfit.text = data.profit
//            tvPersonalExpenses.text = data.personalExpenses
//            tvRemainingProfit.text = data.remainingProfit
//            tvMilkPurchased.text = data.milkPurchased
//            tvMilkSold.text = data.milkSold
//            tvQtyDiff.text = data.quantityDifference
//            tvAvgFat.text = data.averageFat
//            tvAvgLr.text = data.averageLr
//            tvAvgSalePrice.text = data.averageSalePrice
//            tvAvgCostPrice.text = data.averageCostPrice
//            tvAvgPriceDiff.text = data.averagePriceDifference
//        }
//
//
//        // Add rows using item_record_row.xml via ViewBinding
////        data.recordList.forEach { item ->
////            val rowBinding =
////                ItemSaleRecordRowBinding.inflate(inflater, binding.recordContainer, false)
////            rowBinding.tvDate.text = item.date
////            rowBinding.tvQty.text = item.quantity.toString()
////            rowBinding.tvDeduction.text = item.deduction.toRoundedStr()
////            rowBinding.tvNetMilk.text = item.netMilk.toRoundedStr()
////            rowBinding.tvRate.text = item.rate.toString()
////            rowBinding.tvAmount.text = item.amount.toRoundedStr()
////            rowBinding.tvPaid.text = item.paid.toRoundedStr()
////            rowBinding.tvBalance.text = item.balance.toRoundedStr()
////            binding.recordContainer.addView(rowBinding.root)
////        }
//
//        // Measure + layout the root view
//        val view = binding.root
//        val displayMetrics = context.resources.displayMetrics
//        val width = displayMetrics.widthPixels
//        val height = displayMetrics.heightPixels
//        view.measure(
//            View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
//            View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.UNSPECIFIED)
//        )
//        view.layout(0, 0, width, view.measuredHeight)
//
////        // Create PDF
////        // 👇 This call utilizes the improved file naming logic in PdfUtils
////        val baseName = data.receipt.replace(" ", "_").replace(Regex("[^a-zA-Z0-9_-]"), "")
//
//
//        val pdfDoc = PdfDocument()
//        val pageInfo = PdfDocument.PageInfo.Builder(width, view.measuredHeight, 1).create()
//        val page = pdfDoc.startPage(pageInfo)
//
//        view.draw(page.canvas)
//        pdfDoc.finishPage(page)
//
//        FileOutputStream(file).use { pdfDoc.writeTo(it) }
//        pdfDoc.close()
//
//        return file
//    }
//
//    /**
//     * Generates the PDF and immediately initiates the share intent.
//     */
//    fun generateAndSharePdf(
//        context: Context,
//        data: DashboardSummaryPdf,
//        baseName: String,
//        showLogo: Boolean = false,
//        logoResId: Int? = null
//    ) {
//        // Generate the file first
//        val file = generateDashboardPdf(context, data, baseName, showLogo, logoResId)
//
//        // Then share it
//        PdfUtils.sharePdf(context, file)
//    }
}
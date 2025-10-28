package com.miassolutions.milkledger.core.pdf.supplierreport

import android.content.Context
import android.graphics.pdf.PdfDocument
import android.view.LayoutInflater
import android.view.View
import com.miassolutions.milkledger.core.pdf.PdfUtils
import com.miassolutions.milkledger.core.pdf.customerreport.SalesReceiptPdf
import com.miassolutions.milkledger.core.util.toRoundedStr
import com.miassolutions.milkledger.databinding.ItemRecordRowBinding
import com.miassolutions.milkledger.databinding.ItemSaleRecordRowBinding
import com.miassolutions.milkledger.databinding.LayoutPurchaseReceiptBinding
import com.miassolutions.milkledger.databinding.LayoutSalesReceiptBinding
import java.io.File
import java.io.FileOutputStream

object SupplierReportGenerator {

    /**
     * Generates the PDF document from the layout, saves it to a file, and returns the file.
     * The file naming uses the improved logic from PdfUtils (BaseName_YYYYMMDD_HHMMSS.pdf).
     */
    private fun generatePurchaseReceiptPdf(
        context: Context,
        data: SupplierReceiptPdf,
        baseName: String,
        showLogo: Boolean = false,
        logoResId: Int? = null
    ): File {
        val inflater = LayoutInflater.from(context)
        val binding = LayoutPurchaseReceiptBinding.inflate(inflater)

        // Show/hide logo
        if (showLogo && logoResId != null) {
            binding.imgLogo.visibility = View.VISIBLE
            binding.imgLogo.setImageBitmap(PdfUtils.getLogoBitmap(context, logoResId))
        } else {
            binding.imgLogo.visibility = View.GONE
        }
        val file = PdfUtils.getPdfFile(context, baseName)
        val receiptName = file.name.removeSuffix(".pdf")
        // Header info
        with(binding) {
            tvReceipt.text = "Receipt Id: $receiptName"
            tvPartyName.text = "${data.supplierName}"
            tvDateRange.text = "Date Range: ${data.dateRange}"
            tvSummaryTotalQty.text = data.totalQty
            tvAvgTs.text = data.avgTs
            tvSummaryTotalAmount.text = data.totalAmount
            tvSummaryTotalPaid.text = data.totalPaid
            tvSummaryBalance.text = data.totalBalance

            tvFooter.text = data.footerNote ?: ""
        }


        // Add rows using item_record_row.xml via ViewBinding
        data.recordList.forEach { item ->
            val rowBinding = ItemRecordRowBinding.inflate(inflater, binding.recordContainer, false)
            rowBinding.tvDate.text = item.date
            rowBinding.tvQty.text = item.quantity.toString()
            rowBinding.tvTS.text = item.ts.toRoundedStr()
            rowBinding.tvRate.text = item.rate.toString()
            rowBinding.tvAmount.text = item.amount.toRoundedStr()
            rowBinding.tvPaid.text = item.paid.toRoundedStr()
            rowBinding.tvBalance.text = item.balance.toRoundedStr()
            binding.recordContainer.addView(rowBinding.root)
        }

        // Measure + layout the root view
        val view = binding.root
        val displayMetrics = context.resources.displayMetrics
        val width = displayMetrics.widthPixels
        val height = displayMetrics.heightPixels
        view.measure(
            View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.UNSPECIFIED)
        )
        view.layout(0, 0, width, view.measuredHeight)

        val pdfDoc = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(width, view.measuredHeight, 1).create()
        val page = pdfDoc.startPage(pageInfo)

        view.draw(page.canvas)
        pdfDoc.finishPage(page)

        FileOutputStream(file).use { pdfDoc.writeTo(it) }
        pdfDoc.close()

        return file
    }


    /**
     * Generates the PDF document from the layout, saves it to a file, and returns the file.
     * The file naming uses the improved logic from PdfUtils (BaseName_YYYYMMDD_HHMMSS.pdf).
     */
    private fun generateSalesReceiptPdf(
        context: Context,
        data: SalesReceiptPdf,
        baseName: String,
        showLogo: Boolean = false,
        logoResId: Int? = null
    ): File {
        val inflater = LayoutInflater.from(context)
        val binding = LayoutSalesReceiptBinding.inflate(inflater)

        // Show/hide logo
        if (showLogo && logoResId != null) {
            binding.imgLogo.visibility = View.VISIBLE
            binding.imgLogo.setImageBitmap(PdfUtils.getLogoBitmap(context, logoResId))
        } else {
            binding.imgLogo.visibility = View.GONE
        }
        val file = PdfUtils.getPdfFile(context, baseName)
        val receiptName = file.name.removeSuffix(".pdf")
        // Header info
        with(binding) {
            tvReceipt.text = "Receipt Id: $receiptName"
            tvPartyName.text = "Party: ${data.partyName}"
            tvDateRange.text = "Date Range: ${data.dateRange}"
            tvTotal.text = data.totalAmount
            tvTotalPaid.text = data.totalPaid
            tvTotalBalance.text = data.totalBalance
            tvFooter.text = data.footerNote ?: ""
        }


        // Add rows using item_record_row.xml via ViewBinding
        data.recordList.forEach { item ->
            val rowBinding =
                ItemSaleRecordRowBinding.inflate(inflater, binding.recordContainer, false)
            rowBinding.tvDate.text = item.date
            rowBinding.tvQty.text = item.quantity.toString()
            rowBinding.tvDeduction.text = item.deduction.toRoundedStr()
            rowBinding.tvRate.text = item.rate.toString()
            rowBinding.tvAmount.text = item.amount.toRoundedStr()
            rowBinding.tvPaid.text = item.paid.toRoundedStr()
            rowBinding.tvBalance.text = item.balance.toRoundedStr()
            binding.recordContainer.addView(rowBinding.root)
        }

        // Measure + layout the root view
        val view = binding.root
        val displayMetrics = context.resources.displayMetrics
        val width = displayMetrics.widthPixels
        val height = displayMetrics.heightPixels
        view.measure(
            View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.UNSPECIFIED)
        )
        view.layout(0, 0, width, view.measuredHeight)

//        // Create PDF
//        // 👇 This call utilizes the improved file naming logic in PdfUtils
//        val baseName = data.receipt.replace(" ", "_").replace(Regex("[^a-zA-Z0-9_-]"), "")


        val pdfDoc = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(width, view.measuredHeight, 1).create()
        val page = pdfDoc.startPage(pageInfo)

        view.draw(page.canvas)
        pdfDoc.finishPage(page)

        FileOutputStream(file).use { pdfDoc.writeTo(it) }
        pdfDoc.close()

        return file
    }

    /**
     * Generates the PDF and immediately initiates the share intent.
     */
    fun generateAndSharePdf(
        context: Context,
        data: SupplierReceiptPdf,
        baseName: String,
        showLogo: Boolean = false,
        logoResId: Int? = null
    ) {
        // Generate the file first
        val file = generatePurchaseReceiptPdf(context, data, baseName, showLogo, logoResId)

        // Then share it
        PdfUtils.sharePdf(context, file)
    }
}
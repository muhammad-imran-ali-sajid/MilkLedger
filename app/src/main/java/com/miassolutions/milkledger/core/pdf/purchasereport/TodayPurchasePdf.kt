package com.miassolutions.milkledger.core.pdf.purchasereport

import android.content.Context
import android.graphics.pdf.PdfDocument
import android.view.LayoutInflater
import android.view.View
import com.miassolutions.milkledger.core.pdf.PdfUtils
import com.miassolutions.milkledger.databinding.ItemPurchaseRecordRowBinding
// NOTE: I am assuming the purchase layout is bound to LayoutPurchaseReceiptBinding
// If you are using your old LayoutSalesReceiptBinding for the new XML, keep it,
// but I'll use a hypothetical, more appropriate binding name for clarity.
// I will proceed using LayoutSalesReceiptBinding as it's what you provided, but
// will treat it as if it binds to the new receipt_layout.xml.
import com.miassolutions.milkledger.databinding.LayoutTodayPurchaseReceiptBinding
import java.io.File
import java.io.FileOutputStream

object TodayPurchasePdf {

    /**
     * Generates the PDF document from the layout, saves it to a file, and returns the file.
     */
    private fun generateTodayPurchaseReceiptPdf(
        context: Context,
        data: PurchaseReportPdf,
        baseName: String,
        showLogo: Boolean = false,
        logoResId: Int? = null
    ): File {
        val inflater = LayoutInflater.from(context)
        // Using LayoutSalesReceiptBinding as provided in original code,
        // but ensuring IDs match the receipt_layout.xml structure.
        val binding = LayoutTodayPurchaseReceiptBinding.inflate(inflater)

        // Show/hide logo
        if (showLogo && logoResId != null) {
            binding.imgLogo.visibility = View.VISIBLE
            binding.imgLogo.setImageBitmap(PdfUtils.getLogoBitmap(context, logoResId))
        } else {
            binding.imgLogo.visibility = View.GONE
        }

        // Prepare file details
        val file = PdfUtils.getPdfFile(context, baseName)
        val receiptName = file.name.removeSuffix(".pdf")

        // 1. Header and General Info Mapping (based on receipt_layout.xml IDs)
        with(binding) {
            // tv_receipt corresponds to tvPartyName in the header
            tvReceipt.text = "Receipt Id: $receiptName"
            tvPartyName.text = "Milk Purchase Receipt" // Static title
            tvDateRange.text = "Date: ${data.date}" // This ID was tvDateRange
            tvFooter.text = data.footerNote ?: ""
        }


        // 2. Add Detail Rows (assuming ItemSaleRecordRowBinding is adapted for purchase rows)
        data.recordList.forEach { item ->
            val rowBinding =
                ItemPurchaseRecordRowBinding.inflate(inflater, binding.recordContainer, false)

            rowBinding.tvSupplierName.text = item.supplierName
            rowBinding.tvQty.text = item.milkVolume
            rowBinding.tvFat.text = item.fat
            rowBinding.tvLr.text = item.lr
            rowBinding.tvTS.text = item.ts
            rowBinding.tvRate.text = item.rate
            rowBinding.tvAmount.text = item.amount
            rowBinding.tvPaid.text = item.paid
            rowBinding.tvBalance.text = item.balance


            binding.recordContainer.addView(rowBinding.root)
        }

        // 3. Summary Section Mapping (based on the layout created in the previous step)
        // Note: LayoutSalesReceiptBinding needs to have IDs tvSummaryTotalQty, tvSummaryTotalAmount, etc.
        with(binding) {

            tvSummaryTotalQty.text = data.pdfPurchaseSummary.totalQty
            tvSummaryAvgFat.text = data.pdfPurchaseSummary.avgFat
            tvSummaryAvgLr.text = data.pdfPurchaseSummary.avgLr
            tvTotalTs.text = data.pdfPurchaseSummary.totalTs
            tvAvgRate.text = data.pdfPurchaseSummary.avgRate
            tvSummaryTotalAmount.text = data.pdfPurchaseSummary.totalAmount
            tvSummaryTotalPaid.text = data.pdfPurchaseSummary.totalPaid
            tvSummaryBalance.text = data.pdfPurchaseSummary.balanceDue
        }


        // Measure + layout the root view
        val view = binding.root
        val displayMetrics = context.resources.displayMetrics
        val width = displayMetrics.widthPixels
        val height = displayMetrics.heightPixels // Not strictly needed, but kept for context

        // This is crucial for correctly calculating the PDF page height
        view.measure(
            View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.UNSPECIFIED)
        )
        view.layout(0, 0, width, view.measuredHeight)

        // Create PDF
        val pdfDoc = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(width, view.measuredHeight, 1).create()
        val page = pdfDoc.startPage(pageInfo)

        view.draw(page.canvas)
        pdfDoc.finishPage(page)

        // Write to file
        FileOutputStream(file).use { pdfDoc.writeTo(it) }
        pdfDoc.close()

        return file
    }

    /**
     * Generates the PDF and immediately initiates the share intent.
     */
    fun generateAndSharePdf(
        context: Context,
        data: PurchaseReportPdf,
        baseName: String,
        showLogo: Boolean = false,
        logoResId: Int? = null
    ) {
        // Generate the file first
        val file = generateTodayPurchaseReceiptPdf(context, data, baseName, showLogo, logoResId)

        // Then share it
        PdfUtils.sharePdf(context, file)
    }
}
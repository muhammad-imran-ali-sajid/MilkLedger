package com.miassolutions.milkledger.utils.pdf.profitreport

import android.content.Context
import android.graphics.pdf.PdfDocument
import android.view.LayoutInflater
import android.view.View
import com.miassolutions.milkledger.utils.extensions.toPrice
import com.miassolutions.milkledger.databinding.ItemProfitRecordRowBinding
import com.miassolutions.milkledger.databinding.LayoutProfitReceiptBinding
import com.miassolutions.milkledger.utils.pdf.PdfUtils
import java.io.File
import java.io.FileOutputStream

object ProfitReportGenerator {

    private fun generateProfitReceiptPdf(
        context: Context,
        data: ProfitReceiptPdf,
        baseName: String,
        showLogo: Boolean = false,
        logoResId: Int? = null
    ): File {
        val inflater = LayoutInflater.from(context)
        val binding = LayoutProfitReceiptBinding.inflate(inflater)

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


            tvDateRange.text = "Date Range: ${data.dateRange}"
            tvAccGrossProfit.text = data.accGrossProfit
            tvAccNetProfit.text = data.accNetProfit
            tvAccReceived.text = data.accReceived
            tvTotalBalance.text = data.accBalance

        }


        // Add rows using item_record_row.xml via ViewBinding
        data.recordList.forEach { item ->
            val rowBinding =
                ItemProfitRecordRowBinding.inflate(inflater, binding.recordContainer, false)
            rowBinding.tvDate.text = item.date
            rowBinding.tvGrossProfit.text = item.grossProfit.toPrice()
            rowBinding.tvNetProfit.text = item.netProfit.toPrice()
            rowBinding.tvProfitReceived.text = item.profitReceived.toPrice()
            rowBinding.tvBalance.text = item.balance.toPrice()
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
     * Generates the PDF and immediately initiates the share intent.
     */
    fun generateAndSharePdf(
        context: Context,
        data: ProfitReceiptPdf,
        baseName: String,
        showLogo: Boolean = false,
        logoResId: Int? = null
    ) {
        // Generate the file first
        val file = generateProfitReceiptPdf(context, data, baseName, showLogo, logoResId)

        // Then share it
        PdfUtils.sharePdf(context, file)
    }
}
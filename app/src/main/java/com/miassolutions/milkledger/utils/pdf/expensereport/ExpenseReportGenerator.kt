package com.miassolutions.milkledger.utils.pdf.expensereport

import android.content.Context
import android.graphics.pdf.PdfDocument
import android.view.LayoutInflater
import android.view.View
import com.miassolutions.milkledger.utils.extensions.toPriceStr
import com.miassolutions.milkledger.databinding.ItemExpenseRecordRowBinding
import com.miassolutions.milkledger.databinding.LayoutExpenseReceiptBinding
import com.miassolutions.milkledger.utils.pdf.PdfUtils
import java.io.File
import java.io.FileOutputStream

object ExpenseReportGenerator {


    /**
     * Generates the PDF document from the layout, saves it to a file, and returns the file.
     * The file naming uses the improved logic from PdfUtils (BaseName_YYYYMMDD_HHMMSS.pdf).
     */
    private fun generateExpenseReceiptPdf(
        context: Context,
        data: ExpenseReceiptPdf,
        baseName: String,
        showLogo: Boolean = false,
        logoResId: Int? = null
    ): File {
        val inflater = LayoutInflater.from(context)
        val binding = LayoutExpenseReceiptBinding.inflate(inflater)

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
            tvTotalExpense.text = data.totalExpenses.toPriceStr()
            tvTotalPersonalExpense.text = data.personalExpenses.toPriceStr()
            tvTotalBusinessExpense.text = data.businessExpenses.toPriceStr()
//            tvFooter.text = data.footerNote ?: ""
        }


        // Add rows using item_record_row.xml via ViewBinding
        data.recordList.forEach { item ->
            val rowBinding =
                ItemExpenseRecordRowBinding.inflate(inflater, binding.recordContainer, false)
            rowBinding.tvDate.text = item.date
            rowBinding.tvExpenseTitle.text = item.expenseTitle
            rowBinding.tvExpenseType.text = item.expenseType
            rowBinding.tvExpenseAmount.text = item.expenseAmount.toPriceStr()
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
        data: ExpenseReceiptPdf,
        baseName: String,
        showLogo: Boolean = false,
        logoResId: Int? = null
    ) {
        // Generate the file first
        val file = generateExpenseReceiptPdf(context, data, baseName, showLogo, logoResId)

        // Then share it
        PdfUtils.sharePdf(context, file)
    }
}
package com.miassolutions.milkledger.core.pdf

import android.content.Context
import android.graphics.pdf.PdfDocument
import android.view.LayoutInflater
import android.view.View
import com.miassolutions.milkledger.databinding.ItemRecordRowBinding
import com.miassolutions.milkledger.databinding.ReceiptLayoutBinding
import java.io.File
import java.io.FileOutputStream

object PdfViewGenerator {

    fun generateReceiptPdf(
        context: Context,
        data: PdfReceiptData,
        showLogo: Boolean = false,
        logoResId: Int? = null
    ): File {
        val inflater = LayoutInflater.from(context)
        val binding = ReceiptLayoutBinding.inflate(inflater)

        // Show/hide logo
        if (showLogo && logoResId != null) {
            binding.imgLogo.visibility = View.VISIBLE
            binding.imgLogo.setImageBitmap(PdfUtils.getLogoBitmap(context, logoResId))
        } else {
            binding.imgLogo.visibility = View.GONE
        }

        // Header info
        binding.tvTitle.text = data.title
        binding.tvPartyName.text = "Party: ${data.partyName}"
        binding.tvDate.text = "Date: ${PdfUtils.getDateString(data.date)}"
        binding.tvTotal.text = "Total: ${data.totalAmount}"
        binding.tvFooter.text = data.footerNote ?: ""

        // Add rows using item_record_row.xml via ViewBinding
        data.recordList.forEach { item ->
            val rowBinding = ItemRecordRowBinding.inflate(inflater, binding.recordContainer, false)
            rowBinding.tvItemName.text = item.description
            rowBinding.tvQty.text = item.quantity.toString()
            rowBinding.tvRate.text = item.rate.toString()
            rowBinding.tvAmount.text = item.amount.toString()
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

        // Create PDF
        val file = PdfUtils.getPdfFile(context, data.title.replace(" ", "_"))
        val pdfDoc = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(width, view.measuredHeight, 1).create()
        val page = pdfDoc.startPage(pageInfo)

        view.draw(page.canvas)
        pdfDoc.finishPage(page)

        FileOutputStream(file).use { pdfDoc.writeTo(it) }
        pdfDoc.close()

        return file
    }

    fun generateAndSharePdf(
        context: Context,
        data: PdfReceiptData,
        showLogo: Boolean = false,
        logoResId: Int? = null
    ) {
        val file = generateReceiptPdf(context, data, showLogo, logoResId)
        PdfShareHelper.sharePdf(context, file)
    }
}

package com.miassolutions.milkledger.core.pdf

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.pdf.PdfDocument
import android.view.LayoutInflater
import android.view.View
import com.miassolutions.milkledger.databinding.ItemRecordRowBinding
import com.miassolutions.milkledger.databinding.ReceiptLayoutBinding
import java.io.File
import java.io.FileOutputStream
import androidx.core.graphics.createBitmap

object HybridPdfGenerator {

    private const val A4_WIDTH = 595
    private const val A4_HEIGHT = 842

    fun generateReceiptPdf(
        context: Context,
        data: PdfReceiptData,
        showLogo: Boolean = false,
        logoResId: Int? = null
    ): File {
        val inflater = LayoutInflater.from(context)
        val binding = ReceiptLayoutBinding.inflate(inflater)

        // --- 1. Populate the layout
        if (showLogo && logoResId != null) {
            binding.imgLogo.visibility = View.VISIBLE
            binding.imgLogo.setImageBitmap(PdfUtils.getLogoBitmap(context, logoResId))
        } else {
            binding.imgLogo.visibility = View.GONE
        }

        binding.tvTitle.text = data.title
        binding.tvPartyName.text = "Party: ${data.partyName}"
        binding.tvDate.text = "Date: ${PdfUtils.getDateString(data.date)}"
        binding.tvTotal.text = "Total: ${data.totalAmount}"
        binding.tvFooter.text = data.footerNote ?: ""

        // Add rows dynamically
        data.recordList.forEach { item ->
            val rowBinding = ItemRecordRowBinding.inflate(inflater, binding.recordContainer, false)
            rowBinding.tvDate.text = item.date
            rowBinding.tvQty.text = item.quantity.toString()
            rowBinding.tvTS.text = item.ts.toString()
            rowBinding.tvRate.text = item.rate.toString()
            rowBinding.tvAmount.text = item.amount.toString()
            rowBinding.tvPaid.text = item.paid.toString()
            rowBinding.tvBalance.text = item.balance.toString()
            binding.recordContainer.addView(rowBinding.root)
        }

        // --- 2. Measure layout with A4 width (not screen width)
        val view = binding.root
        val widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(A4_WIDTH, View.MeasureSpec.EXACTLY)
        val heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        view.measure(widthMeasureSpec, heightMeasureSpec)
        view.layout(0, 0, A4_WIDTH, view.measuredHeight)

        // --- 3. Create PDF document
        val pdf = PdfDocument()
        var currentPage = 1
        var yOffset = 0

        while (yOffset < view.measuredHeight) {
            val remainingHeight = view.measuredHeight - yOffset
            val pageHeight = minOf(A4_HEIGHT, remainingHeight)

            val pageInfo = PdfDocument.PageInfo.Builder(A4_WIDTH, A4_HEIGHT, currentPage).create()
            val page = pdf.startPage(pageInfo)
            val canvas = page.canvas

            // --- 4. Render a slice of the view to bitmap
            val bitmap = createBitmapFromViewSection(view, yOffset, pageHeight)
            canvas.drawBitmap(bitmap, 0f, 0f, null)

            pdf.finishPage(page)
            yOffset += pageHeight
            currentPage++
        }

        // --- 5. Save file
        val file = PdfUtils.getPdfFile(context, data.title.replace(" ", "_"))
        FileOutputStream(file).use { pdf.writeTo(it) }
        pdf.close()

        return file
    }

    // --- Helper: render part of a View into a bitmap
    private fun createBitmapFromViewSection(view: View, yOffset: Int, height: Int): Bitmap {
        val bitmap = createBitmap(A4_WIDTH, height)
        val canvas = Canvas(bitmap)
        canvas.translate(0f, -yOffset.toFloat())
        view.draw(canvas)
        return bitmap
    }

    fun generateAndSharePdf(
        context: Context,
        data: PdfReceiptData,
        showLogo: Boolean = false,
        logoResId: Int? = null
    ) {
        val file = PdfViewGenerator.generateReceiptPdf(context, data, showLogo, logoResId)
        PdfShareHelper.sharePdf(context, file)
    }
}

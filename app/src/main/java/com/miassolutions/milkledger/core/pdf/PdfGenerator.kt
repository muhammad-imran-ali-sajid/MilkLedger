package com.miassolutions.milkledger.core.pdf

import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import java.io.File
import java.io.FileOutputStream

object PdfGenerator {

    fun createReceiptPdf(context: Context, data: PdfReceiptData): File {
        val file = PdfUtils.getPdfFile(context, data.title.replace(" ", "_"))

        val pdf = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4
        val page = pdf.startPage(pageInfo)
        val canvas = page.canvas

        val paint = Paint().apply {
            color = Color.BLACK
            textSize = 14f
            isAntiAlias = true
        }

        val titlePaint = Paint().apply {
            color = Color.BLACK
            textSize = 20f
            typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        }

        var yPos = 80f
        val xStart = 40f

        // Header
        canvas.drawText(data.title, xStart, yPos, titlePaint)
        yPos += 30f
        canvas.drawText("Date: ${PdfUtils.getDateString(data.date)}", xStart, yPos, paint)
        yPos += 20f
        canvas.drawText("Name: ${data.partyName}", xStart, yPos, paint)
        yPos += 40f

        // Table Headers
        paint.typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        canvas.drawText("Description", xStart, yPos, paint)
        canvas.drawText("Qty", 250f, yPos, paint)
        canvas.drawText("Rate", 350f, yPos, paint)
        canvas.drawText("Amount", 450f, yPos, paint)
        paint.typeface = Typeface.DEFAULT
        yPos += 20f

        // Table Rows
        for (item in data.recordList) {
            canvas.drawText(item.date, xStart, yPos, paint)
            canvas.drawText(item.quantity.toString(), 250f, yPos, paint)
            canvas.drawText(item.rate.toString(), 350f, yPos, paint)
            canvas.drawText(item.amount.toString(), 450f, yPos, paint)
            yPos += 20f
        }

        yPos += 30f
        paint.typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        canvas.drawText("Total: ${data.totalAmount}", 450f, yPos, paint)

        data.footerNote?.let {
            yPos += 40f
            paint.typeface = Typeface.DEFAULT
            paint.textSize = 12f
            canvas.drawText(it, xStart, yPos, paint)
        }

        pdf.finishPage(page)

        FileOutputStream(file).use {
            pdf.writeTo(it)
        }
        pdf.close()

        return file
    }
}
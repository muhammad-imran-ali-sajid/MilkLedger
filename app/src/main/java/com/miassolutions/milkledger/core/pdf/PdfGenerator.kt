package com.miassolutions.milkledger.core.pdf

import android.content.Context
import android.net.Uri
import com.itextpdf.text.*
import com.itextpdf.text.pdf.PdfPCell
import com.itextpdf.text.pdf.PdfPTable
import com.itextpdf.text.pdf.PdfWriter
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStream
import javax.inject.Inject

class PdfGenerator @Inject constructor(@ApplicationContext private val context: Context) {

    private val fontBold = Font(Font.FontFamily.HELVETICA, 12f, Font.BOLD)
    private val fontNormal = Font(Font.FontFamily.HELVETICA, 10f, Font.NORMAL)
    private val fontHeader = Font(Font.FontFamily.HELVETICA, 18f, Font.BOLD)
    private val fontSubHeader =
        Font(Font.FontFamily.HELVETICA, 12f, Font.NORMAL, BaseColor.DARK_GRAY)

    suspend fun generatePdf(uri: Uri, model: PdfReportModel) = withContext(Dispatchers.IO) {
        var outputStream: OutputStream? = null
        try {
            outputStream = context.contentResolver.openOutputStream(uri)

            val document = Document(PageSize.A4)
            PdfWriter.getInstance(document, outputStream)
            document.open()

            // 1. ADD HEADER (Shop Name & Title)
            addHeader(document, model)

            // 2. ADD TABLE
            addTable(document, model)

            // 3. ADD SUMMARY
            addSummary(document, model)

            document.close()
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        } finally {
            outputStream?.close()
        }
    }

    private fun addHeader(document: Document, model: PdfReportModel) {
        val pShop = Paragraph(model.shopName, fontHeader)
        pShop.alignment = Element.ALIGN_CENTER
        document.add(pShop)

        val pTitle = Paragraph(model.reportTitle, fontSubHeader)
        pTitle.alignment = Element.ALIGN_CENTER
        document.add(pTitle)

        val pDate = Paragraph("Date: ${model.dateRange}", fontNormal)
        pDate.alignment = Element.ALIGN_CENTER
        pDate.spacingAfter = 20f
        document.add(pDate)

        document.add(Paragraph("\n")) // Spacer
    }

    private fun addTable(document: Document, model: PdfReportModel) {
        // Setup Table
        val table = PdfPTable(model.columnWeights)
        table.widthPercentage = 100f
        table.spacingBefore = 10f
        table.spacingAfter = 10f

        // A. Headers
        model.columnHeaders.forEach { headerTitle ->
            val cell = PdfPCell(Phrase(headerTitle, fontBold))
            cell.horizontalAlignment = Element.ALIGN_CENTER
            cell.backgroundColor = BaseColor.LIGHT_GRAY
            cell.setPadding(5f)
            table.addCell(cell)
        }

        // B. Data Rows
        model.rows.forEach { rowData ->
            rowData.forEachIndexed { index, cellValue ->
                val cell = PdfPCell(Phrase(cellValue, fontNormal))
                cell.setPadding(5f)

                // Align Numbers to Right, Text to Left
                // (Assuming pehla column Date hai, baki numbers)
                if (index == 0) cell.horizontalAlignment = Element.ALIGN_LEFT
                else cell.horizontalAlignment = Element.ALIGN_RIGHT

                table.addCell(cell)
            }
        }

        document.add(table)
    }

    private fun addSummary(document: Document, model: PdfReportModel) {
        val table = PdfPTable(2) // 2 Columns: Label | Value
        table.widthPercentage = 50f // Screen ki half width
        table.horizontalAlignment = Element.ALIGN_RIGHT // Right side pe show ho

        model.summaryLabels.forEach { (label, value) ->
            val cellLabel = PdfPCell(Phrase(label, fontBold))
            cellLabel.border = Rectangle.NO_BORDER
            cellLabel.horizontalAlignment = Element.ALIGN_RIGHT

            val cellValue = PdfPCell(Phrase(value, fontNormal))
            cellValue.border = Rectangle.NO_BORDER
            cellValue.horizontalAlignment = Element.ALIGN_RIGHT

            table.addCell(cellLabel)
            table.addCell(cellValue)
        }

        document.add(table)
    }
}
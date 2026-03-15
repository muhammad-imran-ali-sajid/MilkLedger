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

    // --- COLORS ---
    private val colorPrimary = BaseColor(41, 98, 255) // Blue
    private val colorLightGray = BaseColor(245, 245, 245)
    private val colorSuccess = BaseColor(46, 125, 50) // Green
    private val colorError = BaseColor(211, 47, 47)   // Red
    private val colorTextWhite = BaseColor.WHITE

    // --- FONTS ---
    private val fontBold = Font(Font.FontFamily.HELVETICA, 12f, Font.BOLD)
    private val fontNormal = Font(Font.FontFamily.HELVETICA, 10f, Font.NORMAL)
    private val fontHeader = Font(Font.FontFamily.HELVETICA, 20f, Font.BOLD, colorPrimary)
    private val fontSubHeader = Font(Font.FontFamily.HELVETICA, 12f, Font.BOLD, BaseColor.DARK_GRAY)
    private val fontWhiteBold = Font(Font.FontFamily.HELVETICA, 12f, Font.BOLD, colorTextWhite)
    private val fontSection = Font(Font.FontFamily.HELVETICA, 11f, Font.BOLD, colorTextWhite)

    suspend fun generatePdf(uri: Uri, model: PdfReportModel) = withContext(Dispatchers.IO) {
        var outputStream: OutputStream? = null
        try {
            outputStream = context.contentResolver.openOutputStream(uri)
            val document = Document(PageSize.A4)
            val writer = PdfWriter.getInstance(document, outputStream)
            writer.pageEvent = PdfFooter()

            document.open()

            addHeader(document, model)
            addTable(document, model)
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
        pTitle.spacingBefore = 5f
        document.add(pTitle)

        val pDate = Paragraph("Period: ${model.dateRange}", fontNormal)
        pDate.alignment = Element.ALIGN_CENTER
        pDate.spacingAfter = 20f
        document.add(pDate)
    }

    private fun addTable(document: Document, model: PdfReportModel) {
        val table = PdfPTable(model.columnWeights)
        table.widthPercentage = 100f
        table.spacingBefore = 10f
        table.spacingAfter = 10f

        // 1. HEADERS
        model.columnHeaders.forEach { headerTitle ->
            val cell = PdfPCell(Phrase(headerTitle, fontWhiteBold))
            cell.horizontalAlignment = Element.ALIGN_CENTER
            cell.backgroundColor = colorPrimary
            cell.paddingTop = 8f
            cell.paddingBottom = 8f
            table.addCell(cell)
        }

        // 2. DATA ROWS
        model.rows.forEachIndexed { rowIndex, rowData ->
            val firstCol = rowData.getOrElse(0) { "" }

            // --- SPECIAL ROW: SECTION HEADER ---
            // Detects if the row is a "Section Divider" (We will set this in Mapper)
            if (firstCol.startsWith("SECTION:")) {
                val cleanTitle = firstCol.removePrefix("SECTION:").trim()
                val cell = PdfPCell(Phrase(cleanTitle, fontSection))
                cell.colspan = model.columnHeaders.size // Merge all columns
                cell.backgroundColor = BaseColor.GRAY // Darker divider
                cell.horizontalAlignment = Element.ALIGN_LEFT
                cell.setPadding(6f)
                table.addCell(cell)
            }
            // --- NORMAL DATA ROWS ---
            else {
                rowData.forEachIndexed { colIndex, cellValue ->
                    // Determine Color for Profit/Loss values
                    val textColor = getValueColor(firstCol, cellValue)
                    val cellFont = Font(Font.FontFamily.HELVETICA, 10f, Font.NORMAL, textColor)

                    val cell = PdfPCell(Phrase(cellValue, cellFont))
                    cell.setPadding(6f)

                    // Zebra Striping (Alternating Gray)
                    if (rowIndex % 2 != 0) {
                        cell.backgroundColor = colorLightGray
                    }

                    // Alignment
                    cell.horizontalAlignment =
                        if (colIndex == 0) Element.ALIGN_LEFT else Element.ALIGN_CENTER
                    table.addCell(cell)
                }
            }
        }

        // 3. TABLE FOOTER (Existing functionality)
        model.summaryRow?.let { footerData ->
            footerData.forEach { cellValue ->
                val cell = PdfPCell(Phrase(cellValue, fontBold))
                cell.horizontalAlignment = Element.ALIGN_CENTER
                cell.backgroundColor = BaseColor.LIGHT_GRAY
                cell.setPadding(6f)
                table.addCell(cell)
            }
        }

        document.add(table)
    }

    private fun addSummary(document: Document, model: PdfReportModel) {
        if (model.summaryLabels.isEmpty()) return

        val table = PdfPTable(2)
        table.widthPercentage = 50f
        table.horizontalAlignment = Element.ALIGN_RIGHT
        table.spacingBefore = 10f

        model.summaryLabels.forEach { (label, value) ->
            // Label
            val cellLabel = PdfPCell(Phrase(label, fontBold))
            cellLabel.border = Rectangle.NO_BORDER
            cellLabel.horizontalAlignment = Element.ALIGN_RIGHT

            // Value (Colorize if it's Profit/Loss)
            val textColor = getValueColor(label, value)
            val valueFont = Font(Font.FontFamily.HELVETICA, 12f, Font.BOLD, textColor)

            val cellValue = PdfPCell(Phrase(value, valueFont))
            cellValue.border = Rectangle.NO_BORDER
            cellValue.horizontalAlignment = Element.ALIGN_RIGHT

            table.addCell(cellLabel)
            table.addCell(cellValue)
        }
        document.add(table)
    }

    // Helper to determine Green/Red/Black text color
    private fun getValueColor(label: String, value: String): BaseColor {
        val isFinancial = label.contains("PROFIT", ignoreCase = true) ||
                label.contains("Diff", ignoreCase = true) ||
                label.contains("Margin", ignoreCase = true)

        if (isFinancial) {
            return when {
                value.contains("-") -> colorError   // Negative = Red
                value == "0" || value == "0.00" -> BaseColor.BLACK
                else -> colorSuccess                // Positive = Green
            }
        }
        return BaseColor.BLACK
    }
}
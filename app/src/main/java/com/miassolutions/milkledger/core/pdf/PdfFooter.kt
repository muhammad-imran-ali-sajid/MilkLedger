package com.miassolutions.milkledger.core.pdf

import com.itextpdf.text.*
import com.itextpdf.text.pdf.*

class PdfFooter : PdfPageEventHelper() {

    private val font = Font(Font.FontFamily.HELVETICA, 9f, Font.NORMAL, BaseColor.GRAY)

    override fun onEndPage(writer: PdfWriter, document: Document) {

        val footerTable = PdfPTable(2)
        footerTable.totalWidth = document.pageSize.width - document.leftMargin() - document.rightMargin()
        footerTable.setWidths(floatArrayOf(3f, 1f))

        // Left side footer
        val left = PdfPCell(Phrase("© 2026 MilkLedger | Developed by M. Imran Ali Sajid", font))
        left.border = Rectangle.NO_BORDER
        left.horizontalAlignment = Element.ALIGN_LEFT

        // Right side page number
        val pageNumber = PdfPCell(Phrase("Page ${writer.pageNumber}", font))
        pageNumber.border = Rectangle.NO_BORDER
        pageNumber.horizontalAlignment = Element.ALIGN_RIGHT

        footerTable.addCell(left)
        footerTable.addCell(pageNumber)

        footerTable.writeSelectedRows(
            0,
            -1,
            document.leftMargin(),
            document.bottomMargin() - 10,
            writer.directContent
        )
    }
}
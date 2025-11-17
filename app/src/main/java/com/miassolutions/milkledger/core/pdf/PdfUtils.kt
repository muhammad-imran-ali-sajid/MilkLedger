package com.miassolutions.milkledger.core.pdf

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.core.content.FileProvider
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object PdfUtils {

    // Add milliseconds for guaranteed unique filenames
    val DATE_FORMATTER = DateTimeFormatter.ofPattern("ddMMyyyy_HHmmss_SSS")

    private const val FOLDER_NAME = "Receipts"

    fun getPdfFile(context: Context, baseName: String): File {
        val dir = File(context.getExternalFilesDir(null), FOLDER_NAME)
        if (!dir.exists()) dir.mkdirs()

        val timestamp = LocalDateTime.now().format(DATE_FORMATTER)
        val fileName = "${baseName}_$timestamp.pdf"

        return File(dir, fileName)
    }

    fun getLogoBitmap(context: Context, resId: Int): Bitmap {
        return BitmapFactory.decodeResource(context.resources, resId)
    }

    fun sharePdf(context: Context, file: File, title: String = "Share Receipt") {

        val uri = FileProvider.getUriForFile(
            context,
            context.packageName + ".provider",
            file
        )

        // Ensure URI permission freshly granted
        context.grantUriPermission(
            context.packageName,
            uri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        Log.d("PDF_UTILS", "Sharing: ${file.absolutePath}")

        context.startActivity(Intent.createChooser(intent, title))
    }
}

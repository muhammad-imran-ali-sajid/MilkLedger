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

    val DATE_FORMATTER = DateTimeFormatter.ofPattern("ddMMyyyy_HHmmss")

    private const val FOLDER_NAME = "Receipts"

    fun getPdfFile(context: Context, baseName : String): File {
        val dir = File(context.getExternalFilesDir(null), FOLDER_NAME)
        // Ensure the directory exists
        if (!dir.exists()) dir.mkdirs()

        // Get the current date and time formatted for a file name
        val timestamp = LocalDateTime.now().format(DATE_FORMATTER)

        // Sanitize the base name for file system compatibility (optional but recommended)
//        val sanitizedBaseName = baseName.replace(Regex("[^a-zA-Z0-9_-]"), "_")

        // Construct the unique file name
        val fileName = "${baseName}_$timestamp.pdf"

        return File(dir, fileName)
    }


    fun getLogoBitmap(context: Context, resId: Int): Bitmap {
        return BitmapFactory.decodeResource(context.resources, resId)
    }


    fun sharePdf(context: Context, file: File, title: String = "Share Receipt") {
        val uri = FileProvider.getUriForFile(
            context,
            context.packageName + ".provider", // defined in manifest
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        Log.d("PDF_UTILS", "${file.absolutePath}")
        context.startActivity(Intent.createChooser(intent, title))
    }
}
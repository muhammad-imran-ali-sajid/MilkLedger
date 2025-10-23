package com.miassolutions.milkledger.core.pdf

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object PdfUtils {

    private const val FOLDER_NAME = "Receipts"

    fun getPdfFile(context: Context, baseName: String): File {
        val dir = File(context.getExternalFilesDir(null), FOLDER_NAME)
        if (!dir.exists()) dir.mkdirs()

        // Automatically generate sequential names
        val files = dir.listFiles()?.filter { it.name.endsWith(".pdf") } ?: emptyList()
        val nextNumber = (files.size + 1).toString().padStart(3, '0')

        return File(dir, "${baseName}_${nextNumber}.pdf")
    }


    fun getLogoBitmap(context: Context, resId: Int): Bitmap {
        return BitmapFactory.decodeResource(context.resources, resId)
    }

//    fun saveBitmapToFile(context: Context, bitmap: Bitmap, name: String): File {
//        val file = File(context.cacheDir, "$name.png")
//        FileOutputStream(file).use { out ->
//            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
//        }
//        return file
//    }
}

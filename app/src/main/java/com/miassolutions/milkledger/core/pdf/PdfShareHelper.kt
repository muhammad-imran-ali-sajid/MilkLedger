package com.miassolutions.milkledger.core.pdf

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File

object PdfShareHelper {

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

        context.startActivity(Intent.createChooser(intent, title))
    }
}
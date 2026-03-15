package com.miassolutions.milkledger.features.activities

import android.app.DownloadManager
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.miassolutions.milkledger.R
import com.miassolutions.milkledger.core.ui.BaseActivity
import com.miassolutions.milkledger.utils.extensions.show
import com.miassolutions.milkledger.databinding.ActivityForceUpdateBinding
import java.io.File

class ForceUpdateActivity : BaseActivity() {

    private val binding by lazy {
        ActivityForceUpdateBinding.inflate(layoutInflater)
    }

    private val apkFileName = "milk-ledger-update.apk"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        onBackPressedDispatcher.addCallback(this) {}

        val message = intent.getStringExtra("message")
        val url = intent.getStringExtra("url")!!

        binding.tvMessage.text = message
        binding.btnDownload.setOnClickListener {
            binding.btnDownload.isEnabled = false
            startDownload(url)

        }


    }

    private fun startDownload(url: String) {
        binding.progressBar.show()
        binding.tvProgress.show()

        val request = DownloadManager.Request(url.toUri())
            .setTitle("Downloading update")
            .setDescription("Please wait...")
            .setDestinationInExternalFilesDir(
                this,
                Environment.DIRECTORY_DOWNLOADS,
                apkFileName
            )
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)

        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        val downloadId = downloadManager.enqueue(request)

        val handler = Handler(Looper.getMainLooper())
        handler.post(object : Runnable {
            override fun run() {
                val query = DownloadManager.Query().setFilterById(downloadId)
                val cursor = downloadManager.query(query)

                if (cursor.moveToFirst()) {
                    val bytesDownloaded =
                        cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                    val bytesTotal =
                        cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))

                    if (bytesTotal > 0) {
                        val progress = (bytesDownloaded * 100L / bytesTotal).toInt()
                        binding.progressBar.progress = progress
                        binding.tvProgress.text = "$progress%"
                    }

                    val status =
                        cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))

                    if (status == DownloadManager.STATUS_SUCCESSFUL) {
                        cursor.close()
                        installApk()
                        return
                    }
                }
                cursor.close()
                handler.postDelayed(this, 500)
            }
        })
    }

    private fun installApk() {
        val apkFile = File(
            getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), apkFileName
        )

        val apkUri = FileProvider.getUriForFile(
            this,
            "${packageName}.provider",
            apkFile
        )

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, "application/vnd.android.package-archive")
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
        }

        startActivity(intent)
    }
}
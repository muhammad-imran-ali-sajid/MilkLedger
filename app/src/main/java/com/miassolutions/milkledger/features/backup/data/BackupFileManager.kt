package com.miassolutions.milkledger.features.backup.data

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupFileManager @Inject constructor(
    @ApplicationContext private val context: Context,
    
) {
    
    fun saveLocalBackupFile(bytes: ByteArray, createdAtMillis: Long): File {
        val backupDir = File(context.filesDir, "milk_ledger_backups")
        if (!backupDir.exists()) backupDir.mkdirs()
        
        val timestamp = SimpleDateFormat(
            "yyyy_MM_dd_HH_mm_ss",
            Locale.US
        ).format(Date(createdAtMillis))
        
        val file = File(
            backupDir,
            "milk_ledger_backup_$timestamp.mlbackup"
        )
        
        file.writeBytes(bytes)
        
        return file
    }
    
    fun getLatestLocalBackupFile(): File? {
        val backupDir = File(context.filesDir, "milk_ledger_backups")
        
        return backupDir
            .listFiles()
            ?.filter { it.extension == "mlbackup" }
            ?.maxByOrNull { it.lastModified() }
    }
    
    fun createDownloadedBackupFile(fileName: String): File {
        val backupDir = File(context.filesDir, "milk_ledger_downloaded_backups")
        if (!backupDir.exists()) backupDir.mkdirs()
        
        val safeFileName = fileName
            .replace("/", "_")
            .replace("\\", "_")
        
        return File(backupDir, safeFileName)
    }
    
    fun getBackupFileByPath(path: String): File {
        return File(path)
    }
}
package com.miassolutions.milkledger.core.backup


import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.*
import androidx.core.content.edit

/**
 * Universal BackupManager that works in any Android project.
 * Handles local database or any file backup/restore safely.
 */
class BackupManager(
    private val context: Context,
    private val targetFile: File,          // the file to back up (e.g. database)
    private val backupDir: File,           // where to store backups
    private val prefsName: String = "backup_prefs"
) {
    private val prefs = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)

    suspend fun createLocalBackup(): File = withContext(Dispatchers.IO) {
        require(targetFile.exists()) { "Target file not found: ${targetFile.path}" }
        if (!backupDir.exists()) backupDir.mkdirs()

        val timestamp = System.currentTimeMillis()
        val backupFile = File(
            backupDir,
            "${targetFile.nameWithoutExtension}_backup_$timestamp.${targetFile.extension}"
        )

        targetFile.copyTo(backupFile, overwrite = true)
        prefs.edit { putLong("last_backup", timestamp) }
        backupFile
    }

    suspend fun restoreFromUri(uri: Uri): Boolean = withContext(Dispatchers.IO) {
        try {
            val docFile = DocumentFile.fromSingleUri(context, uri) ?: return@withContext false
            val input = context.contentResolver.openInputStream(docFile.uri) ?: return@withContext false
            FileOutputStream(targetFile).use { output -> input.copyTo(output) }
            input.close()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun restoreFromFile(file: File): Boolean = withContext(Dispatchers.IO) {
        try {
            if (!file.exists()) return@withContext false
            file.copyTo(targetFile, overwrite = true)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun getAllBackups(): List<File> {
        if (!backupDir.exists()) return emptyList()
        return backupDir.listFiles()
            ?.filter { it.name.startsWith(targetFile.nameWithoutExtension) }
            ?.sortedByDescending { it.lastModified() }
            ?: emptyList()
    }

    fun deleteBackup(file: File): Boolean = file.delete()
    fun deleteAllBackups(): Boolean = getAllBackups().all { it.delete() }
    fun deleteOldBackups(days: Int): Int {
        val cutoff = System.currentTimeMillis() - days * 24 * 60 * 60 * 1000L
        return getAllBackups().count { it.lastModified() < cutoff && it.delete() }
    }

    fun getLastBackupTime(): Long = prefs.getLong("last_backup", 0L)
}

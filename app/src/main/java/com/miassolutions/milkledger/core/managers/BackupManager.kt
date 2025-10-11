package com.miassolutions.milkledger.core.managers


import android.app.Activity
import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.FileContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

/**
 * A reusable backup manager to upload and restore files to/from Google Drive and local storage.
 *
 * @param context Context for accessing file system and content resolver
 * @param backupFile The local file to back up or restore
 * @param backupDir Directory for storing local backups (optional)
 * @param driveAppName App name to use for Drive API client (optional)
 */
class BackupManager(
    private val context: Context,
    private val backupFile: File,
    private val backupDir: File? = null,
    private val driveAppName: String = "MyApp Backup"
) {

    /**
     * Creates Drive service instance with given GoogleAccountCredential.
     */
    private fun getDriveService(credential: GoogleAccountCredential): Drive {
        return Drive.Builder(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        )
            .setApplicationName(driveAppName)
            .build()
    }

    /**
     * Uploads the backupFile to Google Drive.
     *
     * @param activity Used for auth if needed
     * @param credential Authenticated GoogleAccountCredential with proper scopes
     */
    suspend fun uploadToDrive(activity: Activity, credential: GoogleAccountCredential) =
        withContext(Dispatchers.IO) {
            if (!backupFile.exists()) {
                throw IllegalStateException("Backup file does not exist: ${backupFile.absolutePath}")
            }
            val driveService = getDriveService(credential)

            // Prepare file metadata - keep same name and MIME
            val metadata = com.google.api.services.drive.model.File().apply {
                name = backupFile.name
                mimeType = "application/octet-stream"
            }

            val mediaContent = FileContent("application/octet-stream", backupFile)

            // Upload file
            driveService.files()
                .create(metadata, mediaContent)
                .setFields("id")
                .execute()
        }

    /**
     * Restores the backup file from Google Drive by searching for a file with the same name.
     *
     * @param activity Used for auth if needed
     * @param credential Authenticated GoogleAccountCredential with proper scopes
     */
    suspend fun restoreFromDrive(activity: Activity, credential: GoogleAccountCredential) =
        withContext(Dispatchers.IO) {
            val driveService = getDriveService(credential)

            // Query for the file by name in Drive
            val result = driveService.files().list()
                .setQ("name='${backupFile.name}' and trashed=false")
                .setSpaces("drive")
                .setFields("files(id, name)")
                .execute()

            val file = result.files.firstOrNull()
                ?: throw Exception("No backup file '${backupFile.name}' found in Drive.")

            // Download and overwrite local file
            FileOutputStream(backupFile).use { output ->
                driveService.files().get(file.id)
                    .executeMediaAndDownloadTo(output)
            }
        }

    /**
     * Creates a timestamped local backup copy of backupFile in backupDir (if provided),
     * otherwise in the same directory as backupFile.
     *
     * @return The backup copy file
     */
    suspend fun backupLocally(): File = withContext(Dispatchers.IO) {
        val destDir = backupDir ?: backupFile.parentFile
        ?: throw IllegalStateException("No directory to save backup")

        if (!destDir.exists()) destDir.mkdirs()

        val timestamp = System.currentTimeMillis()
        val extension = backupFile.extension.takeIf { it.isNotEmpty() }?.let { ".$it" } ?: ""
        val backupName = "${backupFile.nameWithoutExtension}_backup_$timestamp$extension"
        val backupCopy = File(destDir, backupName)

        FileInputStream(backupFile).channel.use { src ->
            FileOutputStream(backupCopy).channel.use { dst ->
                dst.transferFrom(src, 0, src.size())
            }
        }

        backupCopy
    }

    /**
     * Restores backupFile from a local Uri (e.g. file selected by user).
     *
     * @param uri Uri to the backup file
     * @return true if success, false otherwise
     */
    suspend fun restoreFromLocal(uri: Uri): Boolean = withContext(Dispatchers.IO) {
        try {
            val docFile = DocumentFile.fromSingleUri(context, uri) ?: return@withContext false
            val inputStream = context.contentResolver.openInputStream(docFile.uri) ?: return@withContext false

            FileOutputStream(backupFile).use { output ->
                inputStream.copyTo(output)
            }

            inputStream.close()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }


    suspend fun restoreLatestLocalBackup(): Boolean = withContext(Dispatchers.IO) {
        val dir = backupDir ?: return@withContext false
        if (!dir.exists()) return@withContext false

        val latestBackup = dir.listFiles()
            ?.filter { it.name.startsWith(backupFile.nameWithoutExtension) }
            ?.maxByOrNull { it.lastModified() } ?: return@withContext false

        try {
            FileInputStream(latestBackup).channel.use { src ->
                FileOutputStream(backupFile).channel.use { dst ->
                    dst.transferFrom(src, 0, src.size())
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }


    fun deleteAllBackups(): Boolean {
        val files = getBackupFiles()
        var success = true

        for (file in files) {
            if (!file.delete()) {
                success = false // at least one failed
            }
        }

        return success
    }


    private fun getBackupFiles(): List<File> {
        return backupDir?.listFiles()
            ?.filter { it.name.startsWith(backupFile.nameWithoutExtension) }
            ?.sortedByDescending { it.lastModified() }
            ?: emptyList()
    }


    fun deleteOldBackups(days: Int): Int {
        val cutoff = System.currentTimeMillis() - days * 24 * 60 * 60 * 1000L
        val oldBackups = getBackupFiles().filter { it.lastModified() < cutoff }

        var deletedCount = 0
        for (file in oldBackups) {
            if (file.delete()) deletedCount++
        }

        return deletedCount
    }

    fun deleteBackup(file: File): Boolean {
        return file.exists() && file.delete()
    }

}

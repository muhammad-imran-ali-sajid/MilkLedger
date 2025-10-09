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
import com.miassolutions.milkledger.core.util.Logger
import com.miassolutions.milkledger.core.util.toFormattedString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class BackupManager(private val context: Context) {

    private val dbName = "milk_ledger.db"

    // Path to the local database file
    private val dbPath: File
        get() = context.getDatabasePath(dbName)

    // Directory to save local backups
    private val backupDir: File
        get() = File(context.getExternalFilesDir(null), "Backups")

    /**
     * Creates a Drive service instance using the given GoogleAccountCredential.
     */
    private fun getDriveService(credential: GoogleAccountCredential): Drive {
        return Drive.Builder(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        )
            .setApplicationName("MilkLedger Backup")
            .build()
    }

    /**
     * Uploads the local database backup file to the user's Google Drive.
     *
     * @param activity Activity context for auth purposes if needed
     * @param credential GoogleAccountCredential authenticated for Drive scopes
     */
    suspend fun uploadToDrive(activity: Activity, credential: GoogleAccountCredential) =
        withContext(Dispatchers.IO) {
            try {
                val driveService = getDriveService(credential)
                val localBackup = dbPath

                // Prepare file metadata for Drive
                val metadata = com.google.api.services.drive.model.File().apply {
                    name = "milk_ledger.db"
                    mimeType = "application/octet-stream"
                }

                val mediaContent = FileContent("application/octet-stream", localBackup)

                // Execute file upload
                val uploadedFile = driveService.files()
                    .create(metadata, mediaContent)
                    .setFields("id")
                    .execute()

                Logger.d("Backup uploaded to Drive. File ID: ${uploadedFile.id}", "BackupManager")
            } catch (e: Exception) {
                Logger.e("Upload failed: ${e.message}", "BackupManager")
                throw e
            }
        }

    /**
     * Downloads the latest backup from Google Drive and restores it locally.
     *
     * @param activity Activity context for auth purposes if needed
     * @param credential GoogleAccountCredential authenticated for Drive scopes
     */
    suspend fun restoreFromDrive(activity: Activity, credential: GoogleAccountCredential) =
        withContext(Dispatchers.IO) {
            try {
                val driveService = getDriveService(credential)

                // Query Drive for the backup file by name
                val result = driveService.files().list()
                    .setQ("name='milkflow_backup.db' and trashed=false")
                    .setSpaces("drive")
                    .setFields("files(id, name)")
                    .execute()

                val file = result.files.firstOrNull()
                    ?: throw Exception("No backup found in Drive.")

                // Download the file and overwrite local database
                FileOutputStream(dbPath).use { output ->
                    driveService.files().get(file.id)
                        .executeMediaAndDownloadTo(output)
                }

                Logger.d("Backup restored successfully from Drive.", "BackupManager")
            } catch (e: Exception) {
                Logger.e("Restore failed: ${e.message}", "BackupManager")
                throw e
            }
        }

    /**
     * Creates a local backup copy of the database file with a timestamped filename.
     *
     * @return Backup File on success, null on failure
     */
    suspend fun backupLocally(): File? = withContext(Dispatchers.IO) {
        try {
            if (!backupDir.exists()) backupDir.mkdirs()

            // Backup filename with timestamp prefix
            val backupFile =
                File(backupDir, System.currentTimeMillis().toFormattedString("milk_ledger_db_"))

            // Copy DB file to backup location
            FileInputStream(dbPath).channel.use { src ->
                FileOutputStream(backupFile).channel.use { dst ->
                    dst.transferFrom(src, 0, src.size())
                }
            }

            Logger.d("Local backup saved: ${backupFile.path}", "BackupManager")
            backupFile
        } catch (e: Exception) {
            Logger.e("Local backup failed: ${e.message}", "BackupManager")
            null
        }
    }

    /**
     * Restores the database from a local Uri (e.g., picked from file picker).
     *
     * @param uri Uri pointing to the backup file
     * @return true if restore was successful, false otherwise
     */
    suspend fun restoreFromLocal(uri: Uri): Boolean = withContext(Dispatchers.IO) {
        try {
            val docFile = DocumentFile.fromSingleUri(context, uri) ?: return@withContext false
            val inputStream = context.contentResolver.openInputStream(docFile.uri) ?: return@withContext false

            FileOutputStream(dbPath).use { output ->
                inputStream.copyTo(output)
            }

            inputStream.close()
            Logger.d("Database restored successfully from local", "BackupManager")
            true
        } catch (e: Exception) {
            Logger.e("Local restore failed: ${e.message}", "BackupManager")
            false
        }
    }
}

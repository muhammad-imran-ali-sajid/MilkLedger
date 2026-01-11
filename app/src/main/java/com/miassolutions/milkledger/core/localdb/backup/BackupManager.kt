package com.miassolutions.milkledger.core.localdb.backup

import android.content.Context
import android.net.Uri
import com.miassolutions.milkledger.core.localdb.AppDatabase
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val databaseCloser: DatabaseCloser,
    private val db: AppDatabase
) {

    private val dbName = db.openHelper.databaseName!!

    fun backupTo(uri: Uri): BackupResult = try {
        val dbFile = context.getDatabasePath(dbName)

        context.contentResolver.openOutputStream(uri)?.use { output ->
            dbFile.inputStream().use { it.copyTo(output) }
        }

        BackupResult.Success
    } catch (e: Exception) {
        BackupResult.Error(e.localizedMessage ?: "Backup failed")
    }

    fun restoreFrom(uri: Uri): BackupResult = try {

        // 🔥 1. Close Room safely
        databaseCloser.close()

        val dbFile = context.getDatabasePath(dbName)

        // 🔥 2. Restore main db file
        context.contentResolver.openInputStream(uri)?.use { input ->
            dbFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        // 🔥 3. Delete WAL + SHM
        deleteWalAndShm(dbFile)

        BackupResult.Success
    } catch (e: Exception) {
        BackupResult.Error(e.localizedMessage ?: "Restore failed")
    }

    private fun deleteWalAndShm(dbFile: File) {
        File(dbFile.absolutePath + "-wal").delete()
        File(dbFile.absolutePath + "-shm").delete()
    }
}

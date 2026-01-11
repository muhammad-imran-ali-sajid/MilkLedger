package com.miassolutions.milkledger.core.localdb.backup

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import com.miassolutions.milkledger.core.localdb.AppDatabase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val db: AppDatabase
) {

    private val dbName = db.openHelper.databaseName!!
    private fun dbFile() = context.getDatabasePath(dbName)
    private fun walFile() = File(dbFile().absolutePath + "-wal")
    private fun shmFile() = File(dbFile().absolutePath + "-shm")

    /* ============================================================
       BACKUP (Fixed Stream Issue)
       ============================================================ */

    suspend fun backupTo(uri: Uri): BackupResult = withContext(Dispatchers.IO) {
        return@withContext try {
            // 1️⃣ Ensure WAL content is merged into main DB
            checkpointWal()

            val dbFile = dbFile()
            if (!dbFile.exists()) return@withContext BackupResult.Error("Database file not found")

            // 2️⃣ Open Stream safely once
            context.contentResolver.openOutputStream(uri)?.use { output ->
                dbFile.inputStream().use { input ->
                    input.copyTo(output)
                }
            } ?: return@withContext BackupResult.Error("Unable to access backup location")

            BackupResult.Success

        } catch (e: Exception) {
            e.printStackTrace()
            BackupResult.Error(e.message ?: "Backup failed")
        }
    }

    /* ============================================================
       RESTORE (Fixed Safety Logic)
       ============================================================ */

    suspend fun restoreFrom(uri: Uri): BackupResult = withContext(Dispatchers.IO) {
        return@withContext try {
            val targetDb = dbFile()
            val tempDb = File(context.cacheDir, "$dbName.restore.tmp")

            // 1️⃣ Copy backup into TEMP file first
            context.contentResolver.openInputStream(uri)?.use { input ->
                tempDb.outputStream().use { output ->
                    input.copyTo(output)
                }
            } ?: return@withContext BackupResult.Error("Unable to read backup file")

            // 2️⃣ Integrity check on TEMP file (Safety Check)
            if (!integrityCheck(tempDb)) {
                tempDb.delete()
                return@withContext BackupResult.Error("Backup file is corrupted or invalid")
            }

            // 3️⃣ Close Room completely
            db.close()

            // 4️⃣ Safe Replacement Logic
            // Agar target DB exist karti hai, tabhi delete karein jab naya file ready ho
            if (targetDb.exists()) {
                // Pehly backup le lein (Optional safety, but good)
                val backupSafe = File(context.cacheDir, "$dbName.bak.safety")
                targetDb.copyTo(backupSafe, overwrite = true)

                if (targetDb.delete()) {
                    if (tempDb.renameTo(targetDb)) {
                        // Success! Cleanup safety backup
                        backupSafe.delete()
                    } else {
                        // CRITICAL FAILURE: Restore old DB from safety
                        backupSafe.copyTo(targetDb, overwrite = true)
                        return@withContext BackupResult.Error("System prevented file restore. Reverted to original.")
                    }
                } else {
                    return@withContext BackupResult.Error("Could not clear old database.")
                }
            } else {
                // Agar pehly se DB nahi hai (Fresh Install)
                if (!tempDb.renameTo(targetDb)) {
                    // Fallback using copy if rename fails
                    tempDb.copyTo(targetDb, overwrite = true)
                }
            }

            // Cleanup Temp
            if(tempDb.exists()) tempDb.delete()

            // 5️⃣ Cleanup WAL / SHM (Room will recreate these)
            walFile().delete()
            shmFile().delete()

            BackupResult.Success

        } catch (e: Exception) {
            e.printStackTrace()
            BackupResult.Error(e.message ?: "Restore failed")
        }
    }

    /* ============================================================
       INTERNAL HELPERS
       ============================================================ */

    private fun checkpointWal() {
        try {
            db.openHelper.writableDatabase.query("PRAGMA wal_checkpoint(FULL)").close()
        } catch (e: Exception) {
            // Log error but don't crash, standard DB copy might still work partially
            e.printStackTrace()
        }
    }

    private fun integrityCheck(file: File): Boolean {
        return try {
            SQLiteDatabase.openDatabase(
                file.absolutePath,
                null,
                SQLiteDatabase.OPEN_READONLY
            ).use { sqliteDb ->
                sqliteDb.rawQuery("PRAGMA integrity_check", null).use { cursor ->
                    cursor.moveToFirst() && cursor.getString(0).equals("ok", ignoreCase = true)
                }
            }
        } catch (e: Exception) {
            false
        }
    }
}
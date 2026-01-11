package com.miassolutions.milkledger.core.localdb.backup

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import com.miassolutions.milkledger.core.localdb.AppDatabase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val db: AppDatabase
) {

    private val dbName = db.openHelper.databaseName!! // "milk_ledger_database"
    private fun dbFile() = context.getDatabasePath(dbName)

    // TRUNCATE Mode me WAL/SHM files nahi hotin, lekin purani safai k liye rakh rahy hain
    private fun walFile() = File(dbFile().absolutePath + "-wal")
    private fun shmFile() = File(dbFile().absolutePath + "-shm")

    /* ============================================================
       📥 BACKUP FUNCTION
       Strategy: Since we use TRUNCATE mode, the .db file always
       contains the latest data. We just need to copy it safely.
       ============================================================ */
    suspend fun backupTo(uri: Uri): BackupResult = withContext(Dispatchers.IO) {
        // Synchronized block ensure karta hai k backup k doran koi aur thread interfere na kare
        synchronized(this) {
            return@withContext try {

                // 1. Ensure DB is open (Flush any pending memory operations)
                if (db.isOpen) {
                    // Ek dummy query run krte hain taake ensure ho jaye k connection active hai
                    // aur data file me write ho chuka hai.
                    db.openHelper.readableDatabase.query("SELECT 1").close()
                }

                val sourceFile = dbFile()

                if (!sourceFile.exists()) {
                    return@withContext BackupResult.Error("Database file does not exist.")
                }

                // 2. Copy File to User Selected URI
                context.contentResolver.openOutputStream(uri)?.use { output ->
                    FileInputStream(sourceFile).use { input ->
                        input.copyTo(output)
                    }
                } ?: return@withContext BackupResult.Error("Unable to access backup location.")

                BackupResult.Success

            } catch (e: Exception) {
                e.printStackTrace()
                BackupResult.Error("Backup Failed: ${e.message}")
            }
        }
    }

    /* ============================================================
       📤 RESTORE FUNCTION (Fail-Safe)
       Strategy:
       1. Download to Temp -> 2. Check Integrity -> 3. Backup Current (Safety)
       4. Replace DB -> 5. Restart Helper
       ============================================================ */
    suspend fun restoreFrom(uri: Uri): BackupResult = withContext(Dispatchers.IO) {
        synchronized(this) {
            val currentDb = dbFile()
            val safetyBackup = File(context.cacheDir, "safety_backup.db") // Purana data bachane k liye
            val incomingTemp = File(context.cacheDir, "incoming_restore.tmp") // Naya data check krne k liye

            return@withContext try {

                // 1. Copy Incoming File to Temp (Validation k liye)
                context.contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(incomingTemp).use { output ->
                        input.copyTo(output)
                    }
                } ?: return@withContext BackupResult.Error("Could not read selected file.")

                // 2. Integrity Check (Kya ye valid SQLite file hai?)
                if (!checkIntegrity(incomingTemp)) {
                    incomingTemp.delete()
                    return@withContext BackupResult.Error("File is corrupted or not a valid database.")
                }

                // 3. Create Safety Backup of CURRENT Data (Rollback Plan)
                if (currentDb.exists()) {
                    // Close DB connection before touching files
                    if (db.isOpen) db.close()

                    // Backup le lo
                    currentDb.copyTo(safetyBackup, overwrite = true)
                }

                // 4. Force Close & Delete Old Files
                if (db.isOpen) db.close()
                val deleted = deleteCurrentDbFiles()

                if (!deleted) {
                    // Agar delete fail ho, to wapis safety restore kr den
                    if(safetyBackup.exists()) safetyBackup.copyTo(currentDb, overwrite = true)
                    return@withContext BackupResult.Error("System could not replace old database.")
                }

                // 5. Move New File to Main Location
                if (incomingTemp.renameTo(currentDb)) {
                    // ✅ SUCCESS

                    // Cleanup garbage
                    safetyBackup.delete()
                    walFile().delete() // Purani WAL files bhi ura dein
                    shmFile().delete()

                    BackupResult.Success
                } else {
                    // ❌ FAIL - ROLLBACK
                    // Nayi file move nahi ho saki, purana data wapis lao
                    if (safetyBackup.exists()) {
                        safetyBackup.copyTo(currentDb, overwrite = true)
                    }
                    BackupResult.Error("Restore failed during file replacement.")
                }

            } catch (e: Exception) {
                e.printStackTrace()

                // Critical Failure par bhi Rollback koshish karein
                if (safetyBackup.exists()) {
                    try {
                        safetyBackup.copyTo(currentDb, overwrite = true)
                    } catch (ex: Exception) { ex.printStackTrace() }
                }

                BackupResult.Error("Critical Error: ${e.message}")
            } finally {
                if (incomingTemp.exists()) incomingTemp.delete()
            }
        }
    }

    /* ============================================================
       🛠️ HELPERS
       ============================================================ */

    private fun deleteCurrentDbFiles(): Boolean {
        return try {
            val dbDeleted = !dbFile().exists() || dbFile().delete()
            // TRUNCATE mode me inki zaroorat nahi, par safety k liye delete krna acha hai
            val walDeleted = !walFile().exists() || walFile().delete()
            val shmDeleted = !shmFile().exists() || shmFile().delete()

            dbDeleted && walDeleted && shmDeleted
        } catch (e: Exception) {
            false
        }
    }

    /**
     * SQLite command chala kr check krta hai k file corrupted to nahi
     */
    private fun checkIntegrity(file: File): Boolean {
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
            // Agar file SQLite format ki nahi hai to exception ayega
            false
        }
    }
}
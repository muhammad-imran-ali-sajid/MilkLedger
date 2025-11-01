package com.miassolutions.milkledger.data.backup

import android.content.Context
import android.net.Uri
import android.os.Environment
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import com.google.gson.JsonSerializer
import com.google.gson.reflect.TypeToken
import com.miassolutions.milkledger.data.local.AppDatabase
import com.miassolutions.milkledger.data.local.entities.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabaseBackupHelper @Inject constructor(
    @ApplicationContext private val context: Context,
    private val db: AppDatabase
) {

    private val gson = GsonBuilder()
        .setPrettyPrinting()
        .registerTypeAdapter(LocalDate::class.java, object :
            JsonSerializer<LocalDate>, JsonDeserializer<LocalDate> {
            private val formatter = DateTimeFormatter.ISO_LOCAL_DATE
            override fun serialize(src: LocalDate?, typeOfSrc: java.lang.reflect.Type?, ctx: com.google.gson.JsonSerializationContext?) =
                src?.let { com.google.gson.JsonPrimitive(it.format(formatter)) }

            override fun deserialize(json: com.google.gson.JsonElement?, typeOfT: java.lang.reflect.Type?, ctx: com.google.gson.JsonDeserializationContext?) =
                json?.asString?.let { LocalDate.parse(it, formatter) }
        })
        .create()

    private val backupDir = File(
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
        "MilkLedgerBackups"
    ).apply { if (!exists()) mkdirs() }

    data class BackupData(
        val customers: List<CustomerEntity>,
        val suppliers: List<SupplierEntity>,
        val purchases: List<PurchaseEntity>,
        val sales: List<SalesEntity>,
        val expenses: List<ExpensesEntity>
    )

    // Backup to SAF URI
    suspend fun backupDatabaseToUri(uri: Uri, scope: CoroutineScope) = withContext(Dispatchers.IO) {
        val data = BackupData(
            customers = db.customerDao().getAllSync(),
            suppliers = db.supplierDao().getAllSync(),
            purchases = db.purchaseDao().getAllSync(),
            sales = db.salesDao().getAllSync(),
            expenses = db.expensesDao().getAllSync()
        )
        val json = gson.toJson(data)

        // Write JSON to the SAF URI
        scope.coroutineContext[Job]?.let { job ->
            context.contentResolver.openOutputStream(uri)?.use { out ->
                out.write(json.toByteArray())
                out.flush()
            } ?: throw IOException("Failed to open URI for writing")
        }
    }

    // Backup to a file (existing functionality)
    suspend fun backupDatabase(fileName: String = "milk_ledger_backup.json"): File = withContext(Dispatchers.IO) {
        val data = BackupData(
            customers = db.customerDao().getAllSync(),
            suppliers = db.supplierDao().getAllSync(),
            purchases = db.purchaseDao().getAllSync(),
            sales = db.salesDao().getAllSync(),
            expenses = db.expensesDao().getAllSync()
        )
        val json = gson.toJson(data)
        val file = File(backupDir, fileName)
        FileWriter(file).use { it.write(json) }
        file
    }

    // Restore from a file path (existing functionality)
    suspend fun restoreDatabase(fileName: String = "milk_ledger_backup.json"): Boolean = withContext(Dispatchers.IO) {
        val file = File(backupDir, fileName)
        if (!file.exists()) return@withContext false

        FileReader(file).use { reader ->
            val type = object : TypeToken<BackupData>() {}.type
            val backupData: BackupData = gson.fromJson(reader, type)

            db.runInTransaction {
                db.customerDao().clearAll()
                db.supplierDao().clearAll()
                db.purchaseDao().clearAll()
                db.salesDao().clearAll()
                db.expensesDao().clearAll()

                db.customerDao().insertAll(backupData.customers)
                db.supplierDao().insertAll(backupData.suppliers)
                db.purchaseDao().insertAll(backupData.purchases)
                db.salesDao().insertAll(backupData.sales)
                db.expensesDao().insertAll(backupData.expenses)
            }
        }
        true
    }

    // ✅ New: Restore from InputStream (SAF-compatible)
    suspend fun restoreDatabase(inputStream: InputStream): Boolean = withContext(Dispatchers.IO) {
        inputStream.use { stream ->
            InputStreamReader(stream).use { reader ->
                val type = object : TypeToken<BackupData>() {}.type
                val backupData: BackupData = gson.fromJson(reader, type)

                db.runInTransaction {
                    db.customerDao().clearAll()
                    db.supplierDao().clearAll()
                    db.purchaseDao().clearAll()
                    db.salesDao().clearAll()
                    db.expensesDao().clearAll()

                    db.customerDao().insertAll(backupData.customers)
                    db.supplierDao().insertAll(backupData.suppliers)
                    db.purchaseDao().insertAll(backupData.purchases)
                    db.salesDao().insertAll(backupData.sales)
                    db.expensesDao().insertAll(backupData.expenses)
                }
            }
        }
        true
    }


    fun getBackupPath(): String = backupDir.absolutePath
}

package com.miassolutions.milkledger.data.backup

import android.content.Context
import android.net.Uri
import androidx.room.withTransaction
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.google.gson.reflect.TypeToken
import com.miassolutions.milkledger.data.local.AppDatabase
import com.miassolutions.milkledger.data.local.entities.CustomerEntity
import com.miassolutions.milkledger.data.local.entities.ExpensesEntity
import com.miassolutions.milkledger.data.local.entities.NoteEntity
import com.miassolutions.milkledger.data.local.entities.PurchaseEntity
import com.miassolutions.milkledger.data.local.entities.SalesEntity
import com.miassolutions.milkledger.data.local.entities.SupplierEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.InputStream
import java.lang.reflect.Type
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabaseBackupHelper @Inject constructor(
    private val db: AppDatabase,
    @ApplicationContext private val context: Context
) {

    private val gson = GsonBuilder()
        .setPrettyPrinting()
        .registerTypeAdapter(
            LocalDate::class.java,
            object : JsonSerializer<LocalDate>, JsonDeserializer<LocalDate> {
                private val formatter = DateTimeFormatter.ISO_LOCAL_DATE
                override fun serialize(
                    src: LocalDate?,
                    typeOfSrc: Type?,
                    ctx: JsonSerializationContext?
                ) =
                    src?.let { JsonPrimitive(it.format(formatter)) }

                override fun deserialize(
                    json: JsonElement?,
                    typeOfT: Type?,
                    ctx: JsonDeserializationContext?
                ) =
                    json?.asString?.let { LocalDate.parse(it, formatter) }
            }
        )
        .registerTypeAdapter(
            LocalDateTime::class.java,
            object : JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime> {
                private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
                override fun serialize(
                    src: LocalDateTime?,
                    typeOfSrc: Type?,
                    context: JsonSerializationContext?
                ) = src?.let { JsonPrimitive(it.format(formatter)) }

                override fun deserialize(
                    json: JsonElement?,
                    typeOfT: Type?,
                    context: JsonDeserializationContext?
                ) = json?.asString?.let { LocalDateTime.parse(it, formatter) }
            }
        )
        .create()

    data class BackupData(
        val customers: List<CustomerEntity>,
        val suppliers: List<SupplierEntity>,
        val purchases: List<PurchaseEntity>,
        val sales: List<SalesEntity>,
        val expenses: List<ExpensesEntity>,
        val notes: List<NoteEntity>
    )

    // Backup database to URI
    suspend fun backupDatabaseToUri(
        uri: Uri,
        progressCallback: (Int) -> Unit = {}
    ) = withContext(Dispatchers.IO) {

        val data = BackupData(
            customers = db.customerDao().getAllSync(),
            suppliers = db.supplierDao().getAllSync(),
            purchases = db.purchaseDao().getAllSync(),
            sales = db.salesDao().getAllSync(),
            expenses = db.expensesDao().getAllSync(),
            notes = db.noteDao().getAllSync()
        )

        val json = gson.toJson(data)
        val bytes = json.toByteArray()

        context.contentResolver.openOutputStream(uri)?.use { outStream ->
            val bufferSize = 4096
            var written = 0
            while (written < bytes.size) {
                val toWrite = minOf(bufferSize, bytes.size - written)
                outStream.write(bytes, written, toWrite)
                written += toWrite
                progressCallback((written * 100) / bytes.size)
            }
            outStream.flush()
        } ?: throw IOException("Failed to open URI for writing")
    }

    // Restore database from InputStream
    suspend fun restoreDatabase(
        inputStream: InputStream,
        progressCallback: (Int) -> Unit = {}
    ): Boolean = withContext(Dispatchers.IO) {

        val bytes = inputStream.readBytes()
        val total = bytes.size

        val json = buildString {
            val bufferSize = 4096
            var written = 0
            while (written < total) {
                val toWrite = minOf(bufferSize, total - written)
                append(String(bytes, written, toWrite))
                written += toWrite
                progressCallback((written * 100) / total)
            }
        }

        val type = object : TypeToken<BackupData>() {}.type
        val backupData: BackupData = gson.fromJson(json, type)

        // Suspend-friendly transaction
        db.withTransaction {
            db.customerDao().clearAll()
            db.customerDao().insertAll(backupData.customers)

            db.supplierDao().clearAll()
            db.supplierDao().insertAll(backupData.suppliers)

            db.purchaseDao().clearAll()
            db.purchaseDao().insertAll(backupData.purchases)

            db.salesDao().clearAll()
            db.salesDao().insertAll(backupData.sales)

            db.expensesDao().clearAll()
            db.expensesDao().insertAll(backupData.expenses)

            db.noteDao().clearAll()
            db.noteDao().insertAll(backupData.notes)
        }

        true
    }
}

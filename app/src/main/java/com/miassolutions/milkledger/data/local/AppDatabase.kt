package com.miassolutions.milkledger.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.work.impl.Migration_3_4
import com.miassolutions.milkledger.data.local.daos.CustomerDao
import com.miassolutions.milkledger.data.local.daos.ExpensesDao
import com.miassolutions.milkledger.data.local.daos.NoteDao
import com.miassolutions.milkledger.data.local.daos.ProfitDao
import com.miassolutions.milkledger.data.local.daos.PurchaseDao
import com.miassolutions.milkledger.data.local.daos.ReportsDao
import com.miassolutions.milkledger.presentation.customer.sales.db.SalesDao
import com.miassolutions.milkledger.data.local.daos.StateDao
import com.miassolutions.milkledger.data.local.daos.SupplierDao
import com.miassolutions.milkledger.data.local.entities.CustomerEntity
import com.miassolutions.milkledger.data.local.entities.ExpensesEntity
import com.miassolutions.milkledger.data.local.entities.NoteEntity
import com.miassolutions.milkledger.data.local.entities.ProfitEntity
import com.miassolutions.milkledger.data.local.entities.PurchaseEntity
import com.miassolutions.milkledger.presentation.customer.sales.db.SalesEntity
import com.miassolutions.milkledger.data.local.entities.SupplierEntity

@Database(
    entities = [
        CustomerEntity::class,
        SupplierEntity::class,
        PurchaseEntity::class,
        SalesEntity::class,
        ExpensesEntity::class,
        NoteEntity::class,
        ProfitEntity::class
    ],
    version = 4,
    exportSchema = false
)
@TypeConverters(LocalDateConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun customerDao(): CustomerDao
    abstract fun supplierDao(): SupplierDao
    abstract fun purchaseDao(): PurchaseDao
    abstract fun salesDao(): SalesDao
    abstract fun expensesDao(): ExpensesDao
    abstract fun reportsDao(): ReportsDao
    abstract fun noteDao(): NoteDao
    abstract fun profitDao(): ProfitDao

    abstract fun stateDao(): StateDao


}

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "ALTER TABLE sales_table ADD COLUMN paidDate TEXT"
        )
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS profit_table (
                profitId TEXT NOT NULL PRIMARY KEY,
                receivedDate TEXT NOT NULL,
                netProfit REAL NOT NULL,
                receivedProfit REAL NOT NULL,
                notes TEXT,
                isSynced INTEGER NOT NULL,
                updatedAt TEXT NOT NULL,
                deletedAt TEXT
            )
            """.trimIndent()
        )
    }
}

val Migration_3_4 = object : Migration(3,4){
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            ALTER TABLE profit_table
            ADD COLUMN grossProfit REAL NOT NULL DEFAULT 0.0
        """.trimIndent())
    }
}






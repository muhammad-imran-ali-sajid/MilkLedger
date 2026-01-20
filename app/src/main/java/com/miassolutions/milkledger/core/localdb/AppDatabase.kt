package com.miassolutions.milkledger.core.localdb

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.miassolutions.milkledger.core.localdb.account.local.AccountDao
import com.miassolutions.milkledger.core.localdb.account.local.AccountEntity
import com.miassolutions.milkledger.core.localdb.expense.ExpenseDao
import com.miassolutions.milkledger.core.localdb.expense.ExpenseEntity
import com.miassolutions.milkledger.core.localdb.ledger.FinancialLedgerEntity
import com.miassolutions.milkledger.core.localdb.ledger.LedgerDao
import com.miassolutions.milkledger.core.localdb.milk.MilkDao
import com.miassolutions.milkledger.core.localdb.milk.MilkTransactionEntity
import com.miassolutions.milkledger.features.dashboard.data.DashboardDao
import com.miassolutions.milkledger.features.note.data.local.NoteDao
import com.miassolutions.milkledger.features.note.data.local.NoteEntity

@Database(
    entities = [
        AccountEntity::class,
        MilkTransactionEntity::class,
        FinancialLedgerEntity::class,
        ExpenseEntity::class,
        NoteEntity::class,
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(TransactionTypeConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun accountDao(): AccountDao
    abstract fun milkDao(): MilkDao
    abstract fun ledgerDao(): LedgerDao
    abstract fun expenseDao(): ExpenseDao

    abstract fun noteDao(): NoteDao

    abstract fun dashboardDao(): DashboardDao

    companion object {
        // 🔥 STEP 2: Migration Logic define karen
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // SQL query to add new column safely
                db.execSQL(
                    "ALTER TABLE milk_transactions_table ADD COLUMN paymentDateMillis INTEGER DEFAULT NULL"
                )
            }
        }
    }

}
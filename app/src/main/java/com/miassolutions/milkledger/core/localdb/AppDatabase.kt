package com.miassolutions.milkledger.core.localdb

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
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
    version = 1,
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




}
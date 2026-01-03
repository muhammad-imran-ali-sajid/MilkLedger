package com.miassolutions.milkledger.core.localdb

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.miassolutions.milkledger.core.localdb.account.AccountDao
import com.miassolutions.milkledger.core.localdb.account.AccountEntity
import com.miassolutions.milkledger.core.localdb.expense.ExpenseEntity
import com.miassolutions.milkledger.core.localdb.ledger.FinancialLedgerEntity
import com.miassolutions.milkledger.core.localdb.ledger.LedgerDao
import com.miassolutions.milkledger.core.localdb.milk.MilkDao
import com.miassolutions.milkledger.core.localdb.milk.MilkTransactionEntity
import com.miassolutions.milkledger.features.customer.data.local.CustomerDao
import com.miassolutions.milkledger.features.customer.data.local.CustomerEntity
import com.miassolutions.milkledger.core.localdb.expense.ExpenseDao
import com.miassolutions.milkledger.features.note.data.local.NoteDao
import com.miassolutions.milkledger.features.note.data.local.NoteEntity
import com.miassolutions.milkledger.features.profitwithdrawal.ProfitWithdrawalDao
import com.miassolutions.milkledger.features.profitwithdrawal.ProfitWithdrawalEntity
import com.miassolutions.milkledger.features.purchase.data.PurchaseDao
import com.miassolutions.milkledger.features.purchase.data.PurchaseEntity
import com.miassolutions.milkledger.features.sale.data.local.SaleDao
import com.miassolutions.milkledger.features.sale.data.local.SaleEntity
import com.miassolutions.milkledger.features.supplier.data.local.SupplierDao
import com.miassolutions.milkledger.features.supplier.data.local.SupplierEntity
import com.miassolutions.milkledger.features.transaction.data.TransactionDao
import com.miassolutions.milkledger.features.transaction.data.TransactionEntity

@Database(
    entities = [
        AccountEntity::class,
        MilkTransactionEntity::class,
        FinancialLedgerEntity::class,
        ExpenseEntity::class,

        //will be deleted todo()
        CustomerEntity::class,
        SupplierEntity::class,
        PurchaseEntity::class,
        SaleEntity::class,
        NoteEntity::class,
        ProfitWithdrawalEntity::class,
        TransactionEntity::class
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

    abstract fun customerDao(): CustomerDao
    abstract fun supplierDao(): SupplierDao
    abstract fun purchaseDao(): PurchaseDao
    abstract fun salesDao(): SaleDao
    abstract fun noteDao(): NoteDao
    abstract fun profitWithdrawalDao(): ProfitWithdrawalDao
    abstract fun transactionDao(): TransactionDao




}
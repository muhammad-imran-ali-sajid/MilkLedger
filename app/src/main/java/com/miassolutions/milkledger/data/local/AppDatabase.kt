package com.miassolutions.milkledger.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.miassolutions.milkledger.data.local.daos.CustomerDao
import com.miassolutions.milkledger.data.local.daos.ExpensesDao
import com.miassolutions.milkledger.data.local.daos.NoteDao
import com.miassolutions.milkledger.data.local.daos.ProfitWithdrawalDao
import com.miassolutions.milkledger.data.local.daos.PurchaseDao
import com.miassolutions.milkledger.data.local.daos.SalesDao
import com.miassolutions.milkledger.data.local.daos.StatsDao
import com.miassolutions.milkledger.data.local.daos.SupplierDao
import com.miassolutions.milkledger.data.local.daos.TransactionDao
import com.miassolutions.milkledger.data.local.entities.CustomerEntity
import com.miassolutions.milkledger.data.local.entities.NoteEntity
import com.miassolutions.milkledger.data.local.entities.ProfitWithdrawalEntity
import com.miassolutions.milkledger.data.local.entities.PurchaseEntity
import com.miassolutions.milkledger.data.local.entities.SalesEntity
import com.miassolutions.milkledger.data.local.entities.SupplierEntity
import com.miassolutions.milkledger.data.local.entities.TransactionEntity
import com.miassolutions.milkledger.data.local.entities.ExpensesEntity

@Database(
    entities = [
        CustomerEntity::class,
        SupplierEntity::class,
        PurchaseEntity::class,
        SalesEntity::class,
        ExpensesEntity::class,
        NoteEntity::class,
        ProfitWithdrawalEntity::class,
        TransactionEntity::class
    ],
    version = 1,
    exportSchema = false
)

abstract class AppDatabase : RoomDatabase() {

    abstract fun customerDao(): CustomerDao
    abstract fun supplierDao(): SupplierDao
    abstract fun purchaseDao(): PurchaseDao
    abstract fun salesDao(): SalesDao
    abstract fun expensesDao(): ExpensesDao
    abstract fun noteDao(): NoteDao


    abstract fun profitWithdrawalDao(): ProfitWithdrawalDao

    abstract fun transactionDao(): TransactionDao

    abstract fun stateDao(): StatsDao


}

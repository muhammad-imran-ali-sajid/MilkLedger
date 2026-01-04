package com.miassolutions.milkledger.di

import android.content.Context
import androidx.room.Room
import com.miassolutions.milkledger.core.contstants.Constants.DB_NAME
import com.miassolutions.milkledger.core.localdb.AppDatabase
import com.miassolutions.milkledger.core.localdb.account.local.AccountDao
import com.miassolutions.milkledger.core.localdb.ledger.LedgerDao
import com.miassolutions.milkledger.core.localdb.milk.MilkDao
import com.miassolutions.milkledger.features.customer.data.local.CustomerDao
import com.miassolutions.milkledger.core.localdb.expense.ExpenseDao
import com.miassolutions.milkledger.features.note.data.local.NoteDao
import com.miassolutions.milkledger.features.profitwithdrawal.ProfitWithdrawalDao
import com.miassolutions.milkledger.features.purchase.data.PurchaseDao
import com.miassolutions.milkledger.features.sale.data.local.SaleDao
import com.miassolutions.milkledger.features.supplier.data.local.SupplierDao
import com.miassolutions.milkledger.features.transaction.data.TransactionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {


    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext appContext: Context,
    ): AppDatabase =
        Room.databaseBuilder(appContext, AppDatabase::class.java, DB_NAME)
            .fallbackToDestructiveMigration(true) // todo()
            .build()


    @Provides
    @Singleton
    fun provideAccountDao(database: AppDatabase): AccountDao {
        return database.accountDao()
    }

    @Provides
    @Singleton
    fun provideMilkDao(database: AppDatabase): MilkDao {
        return database.milkDao()
    }

    @Provides
    @Singleton
    fun provideLedgerDao(database: AppDatabase): LedgerDao {
        return database.ledgerDao()
    }

    @Singleton
    @Provides
    fun providesExpensesDao(db: AppDatabase): ExpenseDao = db.expenseDao()


    @Singleton
    @Provides
    fun provideCustomerDao(db: AppDatabase): CustomerDao = db.customerDao()

    @Singleton
    @Provides
    fun provideSupplierDao(db: AppDatabase): SupplierDao = db.supplierDao()

    @Singleton
    @Provides
    fun providePurchaseDao(db: AppDatabase): PurchaseDao = db.purchaseDao()

    @Singleton
    @Provides
    fun provideSalesDao(db: AppDatabase): SaleDao = db.salesDao()


    @Singleton
    @Provides
    fun provideNoteDao(db: AppDatabase): NoteDao = db.noteDao()



    @Singleton
    @Provides
    fun provideTransactionDao(db: AppDatabase): TransactionDao = db.transactionDao()

    @Singleton
    @Provides
    fun provideProfitWithdrawalDao(db: AppDatabase): ProfitWithdrawalDao = db.profitWithdrawalDao()

}
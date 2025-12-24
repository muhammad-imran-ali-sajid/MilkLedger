package com.miassolutions.milkledger.core.di

import android.content.Context
import androidx.room.Room
import com.miassolutions.milkledger.core.contstants.Constants.DB_NAME
import com.miassolutions.milkledger.data.local.AppDatabase
import com.miassolutions.milkledger.data.local.MIGRATION_1_2
import com.miassolutions.milkledger.data.local.MIGRATION_2_3
import com.miassolutions.milkledger.data.local.Migration_3_4
import com.miassolutions.milkledger.data.local.daos.CustomerDao
import com.miassolutions.milkledger.data.local.daos.ExpensesDao
import com.miassolutions.milkledger.data.local.daos.NoteDao
import com.miassolutions.milkledger.data.local.daos.ProfitDao
import com.miassolutions.milkledger.data.local.daos.PurchaseDao
import com.miassolutions.milkledger.data.local.daos.ReportsDao
import com.miassolutions.milkledger.presentation.customer.sales.db.SalesDao
import com.miassolutions.milkledger.data.local.daos.StateDao
import com.miassolutions.milkledger.data.local.daos.SupplierDao
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
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, Migration_3_4)
            .build()

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
    fun provideSalesDao(db: AppDatabase): SalesDao = db.salesDao()

    @Singleton
    @Provides
    fun providesExpensesDao(db: AppDatabase): ExpensesDao = db.expensesDao()

    @Singleton
    @Provides
    fun providesReportsDao(db: AppDatabase): ReportsDao = db.reportsDao()

    @Singleton
    @Provides
    fun provideNoteDao(db: AppDatabase): NoteDao = db.noteDao()

    @Singleton
    @Provides
    fun provideProfitDao(db: AppDatabase): ProfitDao = db.profitDao()

    @Singleton
    @Provides
    fun provideStateDao(db: AppDatabase): StateDao = db.stateDao()


}
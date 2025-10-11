package com.miassolutions.milkledger.core.di

import android.content.Context
import androidx.room.Room
import com.miassolutions.milkledger.core.contstants.Constants.DB_NAME
import com.miassolutions.milkledger.data.local.AppDatabase
import com.miassolutions.milkledger.data.local.daos.CustomerDao
import com.miassolutions.milkledger.data.local.daos.PurchaseEntryDao
import com.miassolutions.milkledger.data.local.daos.SalesEntryDao
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
    fun provideDatabase(@ApplicationContext appContext: Context): AppDatabase =
        Room.databaseBuilder(appContext, AppDatabase::class.java, DB_NAME)
            .fallbackToDestructiveMigration(true) //todo
            .build()

    @Singleton
    @Provides
    fun provideCustomerDao(db: AppDatabase): CustomerDao = db.customerDao()

    @Singleton
    @Provides
    fun provideSupplierDao(db: AppDatabase): SupplierDao = db.supplierDao()

    @Singleton
    @Provides
    fun providePurchaseDao(db: AppDatabase): PurchaseEntryDao = db.purchaseEntryDao()

    @Singleton
    @Provides
    fun provideSalesDao(db: AppDatabase): SalesEntryDao = db.salesEntryDao()
}
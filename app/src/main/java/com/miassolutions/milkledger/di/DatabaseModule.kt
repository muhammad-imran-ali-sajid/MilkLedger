package com.miassolutions.milkledger.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.miassolutions.milkledger.core.contstants.Constants.DB_NAME
import com.miassolutions.milkledger.core.localdb.database.AppDatabase
import com.miassolutions.milkledger.core.localdb.account.local.AccountDao
import com.miassolutions.milkledger.core.localdb.expense.ExpenseDao
import com.miassolutions.milkledger.core.localdb.ledger.LedgerDao
import com.miassolutions.milkledger.core.localdb.milk.MilkDao
import com.miassolutions.milkledger.features.dashboard.data.DashboardDao
import com.miassolutions.milkledger.core.localdb.note.NoteDao
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
            .setJournalMode(RoomDatabase.JournalMode.TRUNCATE)
            .addMigrations(AppDatabase.MIGRATION_1_2)
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
    fun provideNoteDao(db: AppDatabase): NoteDao = db.noteDao()


    @Provides
    @Singleton
    fun provideDashboardDao(db: AppDatabase): DashboardDao {
        return db.dashboardDao()
    }

}
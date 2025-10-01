package com.miassolutions.milkledger.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [CustomerEntity::class, SupplierEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun customerDao(): CustomerDao
    abstract fun supplierDao(): SupplierDao
}
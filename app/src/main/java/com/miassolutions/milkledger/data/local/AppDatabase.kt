package com.miassolutions.milkledger.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.miassolutions.milkledger.data.local.daos.CustomerDao
import com.miassolutions.milkledger.data.local.daos.SupplierDao
import com.miassolutions.milkledger.data.local.entities.CustomerEntity
import com.miassolutions.milkledger.data.local.entities.SupplierEntity

@Database(
    entities = [CustomerEntity::class, SupplierEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun customerDao(): CustomerDao
    abstract fun supplierDao(): SupplierDao
}
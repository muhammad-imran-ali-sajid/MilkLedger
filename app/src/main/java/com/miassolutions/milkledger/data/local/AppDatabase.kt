package com.miassolutions.milkledger.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.miassolutions.milkledger.data.local.daos.CustomerDao
import com.miassolutions.milkledger.data.local.daos.PurchaseEntryDao
import com.miassolutions.milkledger.data.local.daos.SalesEntryDao
import com.miassolutions.milkledger.data.local.daos.SupplierDao
import com.miassolutions.milkledger.data.local.entities.CustomerEntity
import com.miassolutions.milkledger.data.local.entities.PurchaseEntryEntity
import com.miassolutions.milkledger.data.local.entities.SalesEntryEntity
import com.miassolutions.milkledger.data.local.entities.SupplierEntity

@Database(
    entities = [
        CustomerEntity::class,
        SupplierEntity::class,
        PurchaseEntryEntity::class,
        SalesEntryEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(LocalDateConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun customerDao(): CustomerDao
    abstract fun supplierDao(): SupplierDao
    abstract fun purchaseEntryDao(): PurchaseEntryDao
    abstract fun salesEntryDao(): SalesEntryDao
}
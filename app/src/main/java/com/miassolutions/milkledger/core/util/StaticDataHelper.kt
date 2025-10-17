package com.miassolutions.milkledger.core.util

import com.miassolutions.milkledger.data.local.AppDatabase
import com.miassolutions.milkledger.data.local.entities.CustomerEntity
import com.miassolutions.milkledger.data.local.entities.ExpensesEntity
import com.miassolutions.milkledger.data.local.entities.SupplierEntity
import javax.inject.Inject

class StaticDataHelper @Inject constructor(private val database: AppDatabase) {

    suspend fun insertStaticData() {
        val customerDao = database.customerDao()
        val supplierDao = database.supplierDao()
        val expensesDao = database.expensesDao()

        val customers = listOf(
            CustomerEntity(
                customerName = "گھر",
                customerRate = 100.0,
                sortOrder = 1,
                isDefault = true
            ),
            CustomerEntity(
                customerName = "العزیز",
                customerRate = 50.0,
                sortOrder = 2,
                isDefault = true
            ),
            CustomerEntity(
                customerName = "العزیز گھر",
                customerRate = 200.0,
                sortOrder = 3,
                isDefault = true
            ),
            CustomerEntity(
                customerName = "بیکری",
                customerRate = 150.0,
                sortOrder = 4,
                isDefault = true
            ),
            CustomerEntity(
                customerName = "بیکری گھر",
                customerRate = 131.0,
                sortOrder = 5,
                isDefault = true
            )
        )

        val suppliers = listOf(
            SupplierEntity(
                supplierName = "ظفر عباس",
                supplierRate = 145.0,
                sortOrder = 1,
                isDefault = true
            ),
            SupplierEntity(
                supplierName = "زاہد",
                supplierRate = 140.0,
                sortOrder = 2,
                isDefault = true
            ),
            SupplierEntity(
                supplierName = "مظہر",
                supplierRate = 165.0,
                sortOrder = 3,
                isDefault = true
            ),
            SupplierEntity(
                supplierName = "ساجد",
                supplierRate = 145.0,
                sortOrder = 4,
                isDefault = true
            ),
            SupplierEntity(
                supplierName = "سیف",
                supplierRate = 155.0,
                sortOrder = 5,
                isDefault = true
            ),
            SupplierEntity(
                supplierName = "پومی",
                supplierRate = 160.0,
                sortOrder = 6,
                isDefault = true
            ),
            SupplierEntity(
                supplierName = "حافظ لیاقت",
                supplierRate = 162.50,
                sortOrder = 7,
                isDefault = true
            )
        )

        val expenseTypes = listOf(
            ExpensesEntity(expenseTitle = "Fuel", isDefault = true),
            ExpensesEntity(expenseTitle = "Wages", isDefault = true),
            ExpensesEntity(expenseTitle = "Transport", isDefault = true),
            ExpensesEntity(expenseTitle = "Maintenance", isDefault = true),
            ExpensesEntity(expenseTitle = "Miscellaneous", isDefault = true)
        )

        customerDao.insertAll(customers)
        supplierDao.insertAll(suppliers)
        expensesDao.insertAll(expenseTypes)
    }
}

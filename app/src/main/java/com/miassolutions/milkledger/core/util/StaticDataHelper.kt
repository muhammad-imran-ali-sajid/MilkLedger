package com.miassolutions.milkledger.core.util

import com.miassolutions.milkledger.data.local.AppDatabase
import com.miassolutions.milkledger.data.local.entities.CustomerEntity
import com.miassolutions.milkledger.data.local.entities.ExpensesEntity
import com.miassolutions.milkledger.data.local.entities.SupplierEntity
import java.time.LocalDateTime
import javax.inject.Inject

class StaticDataHelper @Inject constructor(private val database: AppDatabase) {

    suspend fun insertStaticData() {
        val customerDao = database.customerDao()
        val supplierDao = database.supplierDao()
        val expensesDao = database.expensesDao()

        val customers = listOf(
            CustomerEntity(
                customerName = "Home",
                customerRate = 100.0,
                sortOrder = 1,
                isDefault = true
            ),
            CustomerEntity(
                customerName = "Al-Aziz",
                customerRate = 50.0,
                sortOrder = 2,
                isDefault = true
            ),
            CustomerEntity(
                customerName = "Al-Aziz Home",
                customerRate = 200.0,
                sortOrder = 3,
                isDefault = true
            ),
            CustomerEntity(
                customerName = "Bakery",
                customerRate = 150.0,
                sortOrder = 4,
                isDefault = true
            ),
            CustomerEntity(
                customerName = "Bakery Home",
                customerRate = 131.0,
                sortOrder = 5,
                isDefault = true
            )
        )

        val suppliers = listOf(
            SupplierEntity(
                supplierName = "Zafar Abbas",
                supplierRate = 145.0,
                sortOrder = 1,
                isDefault = true
            ),
            SupplierEntity(
                supplierName = "Zahid",
                supplierRate = 140.0,
                sortOrder = 2,
                isDefault = true
            ),
            SupplierEntity(
                supplierName = "Mazhar",
                supplierRate = 165.0,
                sortOrder = 3,
                isDefault = true
            ),
            SupplierEntity(
                supplierName = "Sajid",
                supplierRate = 145.0,
                sortOrder = 4,
                isDefault = true
            ),
            SupplierEntity(
                supplierName = "Saif",
                supplierRate = 155.0,
                sortOrder = 5,
                isDefault = true
            ),
            SupplierEntity(
                supplierName = "Pomi",
                supplierRate = 160.0,
                sortOrder = 6,
                isDefault = true
            ),
            SupplierEntity(
                supplierName = "Hafiz Liaqat",
                supplierRate = 162.50,
                sortOrder = 7,
                isDefault = true
            )
        )

        val expenseTypes = listOf(
            ExpensesEntity(
                expenseTitle = "Fuel",
                createdAt = LocalDateTime.now().toString(),
                isDefault = true
            ),
            ExpensesEntity(
                expenseTitle = "Meal",
                createdAt = LocalDateTime.now().toString(),
                isDefault = true
            ),


            )

        customerDao.upsertAll(customers)
        supplierDao.upsertAll(suppliers)
        expensesDao.upsertAll(expenseTypes)
    }
}

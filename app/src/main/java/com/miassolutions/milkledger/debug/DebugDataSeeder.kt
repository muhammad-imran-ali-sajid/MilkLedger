package com.miassolutions.milkledger.debug

import com.miassolutions.milkledger.core.contstants.Constants
import com.miassolutions.milkledger.core.localdb.account.local.AccountDao
import com.miassolutions.milkledger.core.localdb.account.local.AccountEntity
import com.miassolutions.milkledger.core.localdb.account.local.AccountType
import com.miassolutions.milkledger.features.expense.data.repository.ExpenseRepository
import com.miassolutions.milkledger.features.expense.domain.Expense
import com.miassolutions.milkledger.features.owner.data.OwnerRepository
import com.miassolutions.milkledger.features.purchase.data.MilkPurchaseRepository
import com.miassolutions.milkledger.features.sale.data.MilkSaleRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject
import kotlin.random.Random

class DebugDataSeeder @Inject constructor(
    private val accountDao: AccountDao,
    private val milkSaleRepository: MilkSaleRepository,
    private val milkPurchaseRepository: MilkPurchaseRepository,
    private val expenseRepository: ExpenseRepository,
    private val ownerRepository: OwnerRepository
) {

    suspend fun seedDummyData() = withContext(Dispatchers.IO) {

        // 1. Setup Accounts (Customers & Suppliers)
        val customer1 = "CUST_ALI"
        val customer2 = "CUST_HAMZA"
        val supplier1 = "SUP_CHACHA"
        val supplier2 = "SUP_FARM"

        val accounts = listOf(
            createAccount(customer1, "Ali General Store", "03001234567", AccountType.CUSTOMER),
            createAccount(customer2, "Hamza Dairy", "03217654321", AccountType.CUSTOMER),
            createAccount(supplier1, "Chacha Gamu", "03011122334", AccountType.SUPPLIER),
            createAccount(supplier2, "Bismillah Farm", "03459988776", AccountType.SUPPLIER)
        )
        // Insert (Ignore if exists)
        accounts.forEach { accountDao.insertIgnore(it) }

        // 2. Generate Data for Past 15 Days
        val today = LocalDate.now()

        for (i in 15 downTo 0) {
            val date = today.minusDays(i.toLong())
            val isEvenDay = (i % 2 == 0)

            // ---------------------------------------------------
            // SCENARIO A: PURCHASE (Doodh khareeda)
            // ---------------------------------------------------
            // Supplier 1 se rozana doodh ata hai
            milkPurchaseRepository.saveMilkPurchase(
                supplierId = supplier1,
                date = date,
                paymentDate = date,
                volume = Random.nextDouble(40.0, 60.0), // 40-60 Liters
                fat = Random.nextDouble(4.5, 5.5),      // 4.5 - 5.5 Fat
                lr = Random.nextDouble(28.0, 30.0),     // 28 - 30 LR
                rate = 180.0,                           // Rate
                amountPaid = if (isEvenDay) 500000 else 0, // Kabhi paise diye, kabhi nahi (5000 PKR)
                note = "Subha ka doodh"
            )

            // Supplier 2 se kabhi kabhi ata hai (Bulk)
            if (i % 3 == 0) {
                milkPurchaseRepository.saveMilkPurchase(
                    supplierId = supplier2,
                    date = date,
                    paymentDate = date,
                    volume = 120.0,
                    fat = 6.0,
                    lr = 29.0,
                    rate = 190.0,
                    amountPaid = 2000000, // 20,000 PKR Paid
                    note = "Bulk Farm Milk"
                )
            }

            // ---------------------------------------------------
            // SCENARIO B: SALE (Doodh becha)
            // ---------------------------------------------------
            // Customer 1 (Ali) rozana le jata hai
            milkSaleRepository.saveMilkSale(
                saleDate = date,
                paymentDate = date,
                accountId = customer1,
                volume = Random.nextDouble(30.0, 45.0),
                deduction = 0.5,
                rate = 220.0,
                amountPaid = if (i % 5 == 0) 0 else 800000, // Kabhi udhaar, kabhi cash (8000 PKR)
                note = "Normal supply"
            )

            // Customer 2 (Hamza)
            if (isEvenDay) {
                milkSaleRepository.saveMilkSale(
                    saleDate = date,
                    paymentDate = date,
                    accountId = customer2,
                    volume = 100.0,
                    deduction = 1.0,
                    rate = 215.0,
                    amountPaid = 1500000, // 15,000 PKR
                    note = "Evening supply"
                )
            }

            // ---------------------------------------------------
            // SCENARIO C: EXPENSES (Karobar k kharchay)
            // ---------------------------------------------------
            // Rozana Chai/Pani ka kharcha
            val teaExpense = Expense(
                expenseId = UUID.randomUUID().toString(),
                date = date,
                amount = 30000, // 300 PKR
                title = "Refreshment",
                note = "Tea for staff",
                category = "Food", // Agar category logic simple hai
                isPersonal = false
            )
            expenseRepository.saveExpense(teaExpense)

            // Hafte me aik baar Petrol/Fuel
            if (i % 7 == 0) {
                val fuelExpense = Expense(
                    expenseId = UUID.randomUUID().toString(),
                    date = date,
                    amount = 150000, // 1500 PKR
                    title = "Bike Fuel",
                    note = "Shop Delivery Bike",
                    isPersonal = false,
                    category = "Transport"
                )
                expenseRepository.saveExpense(fuelExpense)
            }

            // ---------------------------------------------------
            // SCENARIO D: WITHDRAWAL (Ghar k liye paise)
            // ---------------------------------------------------
            // Owner har 5 din baad paise nikalta hai
            if (i % 5 == 0) {
                ownerRepository.saveCashWithdrawal(
                    amount = 500000, // 5000 PKR
                    date = date,
                    note = "Ghar ka kharcha"
                )
            }
        }
    }

    private fun createAccount(
        id: String,
        name: String,
        phone: String,
        type: AccountType
    ): AccountEntity {
        return AccountEntity(
            accountId = id,
            name = name,
            phone = phone,
            accountType = type, // CUSTOMER or SUPPLIER
            initialBalance = 0,
            isActive = true,
            createdAtMillis = System.currentTimeMillis(),
            advanceAmount = 0
        )
    }
}
package com.miassolutions.milkledger.data.repositories

import com.miassolutions.milkledger.data.local.daos.PurchaseDao
import com.miassolutions.milkledger.data.local.entities.PurchaseEntity
import com.miassolutions.milkledger.data.local.entities.SupplierEntity
import com.miassolutions.milkledger.data.local.relations.PurchaseWithSupplier
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PurchaseRepository @Inject constructor(
    private val purchaseEntryDao: PurchaseDao
) {


    suspend fun getPurchasesByDateOnce(date: LocalDate): List<PurchaseWithSupplier> =
        purchaseEntryDao.getPurchasesByDateOnce(date)

    fun getAllSuppliers(): Flow<List<SupplierEntity>> =
        purchaseEntryDao.getAllSuppliers()

    // 🧾 All purchases for reports or dashboard
    fun getAllPurchasesWithSuppliers(): Flow<List<PurchaseWithSupplier>> =
        purchaseEntryDao.getAllPurchasesWithSuppliers()

    // 📅 For current date screen (daily ledger)
    fun getPurchasesByDate(date: LocalDate): Flow<List<PurchaseWithSupplier>> =
        purchaseEntryDao.getPurchasesByDate(date)

    // 👤 For supplier ledger details
    fun getPurchasesForSupplier(supplierId: String): Flow<List<PurchaseWithSupplier>> =
        purchaseEntryDao.getPurchasesForSupplier(supplierId)

    // 🟢 Insert new purchase (used when a supplier first added today)
    suspend fun insertPurchase(purchase: PurchaseEntity) =
        purchaseEntryDao.insertPurchase(purchase)

    // 🟡 Update live changes (fat, lr, volume, notes)
    suspend fun updatePurchase(purchase: PurchaseEntity) =
        purchaseEntryDao.updatePurchase(purchase)

    // 🔴 Delete entry
    suspend fun deletePurchase(purchaseId: String) =
        purchaseEntryDao.deletePurchase(purchaseId)
}

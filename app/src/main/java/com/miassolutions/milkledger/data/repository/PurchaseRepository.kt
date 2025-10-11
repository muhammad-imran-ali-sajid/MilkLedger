package com.miassolutions.milkledger.data.repositories

import com.miassolutions.milkledger.data.local.daos.PurchaseEntryDao
import com.miassolutions.milkledger.data.local.entities.PurchaseEntryEntity
import com.miassolutions.milkledger.data.local.entities.SupplierEntity
import com.miassolutions.milkledger.data.local.relations.PurchaseWithSupplier
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PurchaseRepository @Inject constructor(
    private val purchaseEntryDao: PurchaseEntryDao
) {


    suspend fun getPurchasesByDateOnce(date: LocalDate): List<PurchaseWithSupplier> =
        purchaseEntryDao.getPurchasesByDateOnce(date)

    suspend fun getAllSuppliers(): List<SupplierEntity> =
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
    suspend fun insertPurchase(purchase: PurchaseEntryEntity) =
        purchaseEntryDao.insertPurchase(purchase)

    // 🟡 Update live changes (fat, lr, volume, notes)
    suspend fun updatePurchase(purchase: PurchaseEntryEntity) =
        purchaseEntryDao.updatePurchase(purchase)

    // 🔴 Delete entry
    suspend fun deletePurchase(purchaseId: String) =
        purchaseEntryDao.deletePurchase(purchaseId)
}

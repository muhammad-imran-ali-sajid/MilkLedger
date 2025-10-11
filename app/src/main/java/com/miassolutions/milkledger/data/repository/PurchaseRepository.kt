package com.miassolutions.milkledger.data.repository

import com.miassolutions.milkledger.data.local.daos.PurchaseEntryDao
import com.miassolutions.milkledger.data.local.entities.PurchaseEntryEntity
import com.miassolutions.milkledger.data.local.relations.PurchaseWithSupplier
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PurchaseRepository @Inject constructor(
    private val purchaseEntryDao: PurchaseEntryDao
) {

    fun getAllPurchasesWithSuppliers(): Flow<List<PurchaseWithSupplier>> =
        purchaseEntryDao.getAllPurchasesWithSuppliers()

    fun getPurchasesByDate(date: LocalDate): Flow<List<PurchaseWithSupplier>> =
        purchaseEntryDao.getPurchasesByDate(date)

    fun getPurchasesForSupplier(supplierId: String): Flow<List<PurchaseWithSupplier>> =
        purchaseEntryDao.getPurchasesForSupplier(supplierId)

    suspend fun insertPurchase(purchase: PurchaseEntryEntity) =
        purchaseEntryDao.insertPurchase(purchase)

    suspend fun deletePurchase(purchaseId: String) =
        purchaseEntryDao.deletePurchase(purchaseId)
}
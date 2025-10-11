package com.miassolutions.milkledger.data.local.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.miassolutions.milkledger.data.local.entities.PurchaseEntryEntity
import com.miassolutions.milkledger.data.local.relations.PurchaseWithSupplier
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface PurchaseEntryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchase(purchase: PurchaseEntryEntity)

    @Transaction
    @Query("SELECT * FROM purchase_entry_table ORDER BY date DESC")
    fun getAllPurchasesWithSuppliers(): Flow<List<PurchaseWithSupplier>>

    @Transaction
    @Query("SELECT * FROM purchase_entry_table WHERE date = :date ORDER BY supplierId")
    fun getPurchasesByDate(date: LocalDate): Flow<List<PurchaseWithSupplier>>

    @Transaction
    @Query("SELECT * FROM purchase_entry_table WHERE supplierId = :supplierId ORDER BY date DESC")
    fun getPurchasesForSupplier(supplierId: String): Flow<List<PurchaseWithSupplier>>

    @Query("DELETE FROM purchase_entry_table WHERE purchaseId = :id")
    suspend fun deletePurchase(id: String)
}

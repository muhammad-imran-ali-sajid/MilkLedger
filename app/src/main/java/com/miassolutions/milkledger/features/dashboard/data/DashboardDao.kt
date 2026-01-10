package com.miassolutions.milkledger.features.dashboard.data


import androidx.room.Dao
import androidx.room.Query
import com.miassolutions.milkledger.features.dashboard.model.PurchaseStats
import com.miassolutions.milkledger.features.dashboard.model.SaleStats

import kotlinx.coroutines.flow.Flow

@Dao
interface DashboardDao {

    // 1️⃣ Purchase Stats (Sum & Average)
    // TransactionType.PURCHASE = 'PURCHASE' (Room enum ko string k tor par dekhta hai)
    @Query("""
        SELECT 
            COALESCE(SUM(totalAmount), 0) as totalAmount,
            COALESCE(SUM(volume), 0.0) as totalVolume,
            COALESCE(AVG(fat), 0.0) as avgFat,
            COALESCE(AVG(lr), 0.0) as avgLr,
            COALESCE(AVG(ts), 0.0) as avgTs
        FROM milk_transactions_table
        WHERE dateMillis BETWEEN :start AND :end
        AND type = 'PURCHASE'
        AND deletedAtMillis IS NULL
    """)
    fun getPurchaseStats(start: Long, end: Long): Flow<PurchaseStats>

    // 2️⃣ Sale Stats (Sum Only)
    @Query("""
        SELECT 
            COALESCE(SUM(totalAmount), 0) as totalAmount,
            COALESCE(SUM(volume), 0.0) as totalVolume
        FROM milk_transactions_table
        WHERE dateMillis BETWEEN :start AND :end
        AND type = 'SALE'
        AND deletedAtMillis IS NULL
    """)
    fun getSaleStats(start: Long, end: Long): Flow<SaleStats>

    // 3️⃣ Business Expense Stats (Not Personal)
    @Query("""
        SELECT COALESCE(SUM(amount), 0)
        FROM expense_table
        WHERE dateMillis BETWEEN :start AND :end
        AND isPersonal = 0 
        AND deletedAtMillis IS NULL
    """)
    fun getBusinessExpenseTotal(start: Long, end: Long): Flow<Long>
}
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
//    @Query(
//        """
//    SELECT
//        COALESCE(SUM(totalAmount), 0) as totalAmount,
//        COALESCE(SUM(volume), 0.0) as totalVolume,
//
//        -- 🔥 FIX: Sirf wo entries shamil karo jahan Fat > 0 hai
//        -- CASE WHEN: Agar Fat > 0 hai to value lo, warna NULL kar do (AVG NULL ko ignore karta hai)
//        COALESCE(AVG(CASE WHEN fat > 0 THEN fat ELSE NULL END), 0.0) as avgFat,
//
//        -- 🔥 FIX: LR k liye bhi same logic
//        COALESCE(AVG(CASE WHEN lr > 0 THEN lr ELSE NULL END), 0.0) as avgLr,
//
//        -- 🔥 FIX: TS k liye bhi same logic
//        COALESCE(AVG(CASE WHEN ts > 0 THEN ts ELSE NULL END), 0.0) as avgTs
//
//    FROM milk_transactions_table
//    WHERE dateMillis BETWEEN :start AND :end
//    AND type = 'PURCHASE'
//    AND deletedAtMillis IS NULL
//"""
//    )
//    fun getPurchaseStats(start: Long, end: Long): Flow<PurchaseStats>


    /* Weightage Average Query */
    @Query("""
    SELECT 
        -- 1. Total Amount & Volume (Simple Sum)
        COALESCE(SUM(totalAmount), 0) as totalAmount,
        COALESCE(SUM(volume), 0.0) as totalVolume,

        -- 2. Weighted Average FAT
        -- Formula: Sum(Volume * Fat) / Sum(Volume jinki Fat > 0 thi)
        COALESCE(
            SUM(CASE WHEN fat > 0 THEN (volume * fat) ELSE 0 END) / 
            NULLIF(SUM(CASE WHEN fat > 0 THEN volume ELSE 0 END), 0)
        , 0.0) as avgFat,

        -- 3. Weighted Average LR
        COALESCE(
            SUM(CASE WHEN lr > 0 THEN (volume * lr) ELSE 0 END) / 
            NULLIF(SUM(CASE WHEN lr > 0 THEN volume ELSE 0 END), 0)
        , 0.0) as avgLr,

        -- 4. Weighted Average TS
        COALESCE(
            SUM(CASE WHEN ts > 0 THEN (volume * ts) ELSE 0 END) / 
            NULLIF(SUM(CASE WHEN ts > 0 THEN volume ELSE 0 END), 0)
        , 0.0) as avgTs

    FROM milk_transactions_table
    WHERE dateMillis BETWEEN :start AND :end
    AND type = 'PURCHASE'
    AND deletedAtMillis IS NULL
""")
    fun getPurchaseStats(start: Long, end: Long): Flow<PurchaseStats>

    // 2️⃣ Sale Stats (Sum Only)
    @Query(
        """
        SELECT 
            COALESCE(SUM(totalAmount), 0) as totalAmount,
            COALESCE(SUM(volume), 0.0) as totalVolume
        FROM milk_transactions_table
        WHERE dateMillis BETWEEN :start AND :end
        AND type = 'SALE'
        AND deletedAtMillis IS NULL
    """
    )
    fun getSaleStats(start: Long, end: Long): Flow<SaleStats>

    // 3️⃣ Business Expense Stats (Not Personal)
    @Query(
        """
        SELECT COALESCE(SUM(amount), 0)
        FROM expense_table
        WHERE dateMillis BETWEEN :start AND :end
        AND isPersonal = 0 
        AND deletedAtMillis IS NULL
    """
    )
    fun getBusinessExpenseTotal(start: Long, end: Long): Flow<Long>
}
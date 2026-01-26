package com.miassolutions.milkledger.features.dashboard.data


import androidx.room.Dao
import androidx.room.Query
import com.miassolutions.milkledger.features.dashboard.model.PurchaseStats
import com.miassolutions.milkledger.features.dashboard.model.SaleStats

import kotlinx.coroutines.flow.Flow

@Dao
interface DashboardDao {




    @Query("""
SELECT 
    -- 1. Totals
    COALESCE(SUM(totalAmount), 0) AS totalAmount,
    COALESCE(SUM(volume), 0.0) AS totalVolume,

    -- 2. Weighted Average FAT
    COALESCE(
        SUM(CASE WHEN fat > 0 THEN fat * volume ELSE 0 END) /
        NULLIF(SUM(CASE WHEN fat > 0 THEN volume ELSE 0 END), 0),
        0.0
    ) AS avgFat,

    -- 3. Weighted Average LR
    COALESCE(
        SUM(CASE WHEN fat > 0 THEN lr * volume ELSE 0 END) /
        NULLIF(SUM(CASE WHEN fat > 0 THEN volume ELSE 0 END), 0),
        0.0
    ) AS avgLr,

    -- 4. TOTAL TS
    COALESCE(SUM(CASE WHEN ts > 0 THEN ts ELSE 0 END), 0.0) AS totalTs,

    -- 5. Quality Coverage Volume
    COALESCE(SUM(CASE WHEN fat > 0 THEN volume ELSE 0 END), 0.0) AS qualityVolume

FROM milk_transactions_table
WHERE dateMillis BETWEEN :start AND :end
  AND type = 'PURCHASE'
  AND deletedAtMillis IS NULL
""")
    fun getPurchaseStats(
        start: Long,
        end: Long
    ): Flow<PurchaseStats>




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
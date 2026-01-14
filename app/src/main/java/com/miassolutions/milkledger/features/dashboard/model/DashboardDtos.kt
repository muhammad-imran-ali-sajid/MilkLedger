package com.miassolutions.milkledger.features.dashboard.model


import androidx.room.ColumnInfo

// Purchase ka data hold karne k liye
data class PurchaseStats(
    @ColumnInfo(name = "totalAmount") val totalAmount: Long = 0,
    @ColumnInfo(name = "totalVolume") val totalVolume: Double = 0.0,
    @ColumnInfo(name = "avgFat") val avgFat: Double = 0.0,
    @ColumnInfo(name = "avgLr") val avgLr: Double = 0.0,
    @ColumnInfo(name = "avgTs") val avgTs: Double = 0.0
)

// Sale ka data hold karne k liye
data class SaleStats(
    @ColumnInfo(name = "totalAmount") val totalAmount: Long,
    @ColumnInfo(name = "totalVolume") val totalVolume: Double
)
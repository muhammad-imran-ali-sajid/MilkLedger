package com.miassolutions.milkledger.features.dashboard.model


import androidx.room.ColumnInfo

// Purchase ka data hold karne k liye
data class PurchaseStats(
    @ColumnInfo(name = "totalAmount") val totalAmount: Long,
    @ColumnInfo(name = "totalVolume") val totalVolume: Double,
    @ColumnInfo(name = "avgFat") val avgFat: Double,
    @ColumnInfo(name = "avgLr") val avgLr: Double,
    @ColumnInfo(name = "avgTs") val avgTs: Double
)

// Sale ka data hold karne k liye
data class SaleStats(
    @ColumnInfo(name = "totalAmount") val totalAmount: Long,
    @ColumnInfo(name = "totalVolume") val totalVolume: Double
)
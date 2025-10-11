package com.miassolutions.milkledger.core.util


object MilkCalculationUtils {

    // Total Solids formula (example)
    fun calculateTS(fat: Double, lr: Double, volume: Double): Double {
        // Standard dairy calculation (you can fine-tune later)
        val snf = 0.72 + (lr * 0.25) + (fat * 0.22)
        val ts = (snf + fat * volume) / 13.0
        return ts
//        return (fat / 0.22) + (lr / 4.0)
    }

    // Price formula using rate snapshot
    fun calculatePrice(volume: Double, fat: Double, lr: Double, rate: Double): Double {
        val ts = calculateTS(fat, lr, volume)
        return rate * ts
    }
}

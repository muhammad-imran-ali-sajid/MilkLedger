package com.miassolutions.milkledger.core.util


object MilkCalculationUtils {

    // Total Solids formula (example)
    fun calculateTS(fat: Double, lr: Double): Double {
        // Standard dairy calculation (you can fine-tune later)
        return (fat / 0.22) + (lr / 4.0)
    }

    // Price formula using rate snapshot
    fun calculatePrice(volume: Double, fat: Double, lr: Double, rate: Double): Double {
        val ts = calculateTS(fat, lr)
        return volume * rate * (ts / 100.0)
    }
}

package com.miassolutions.milkledger.core.util


object MilkCalculationUtils {

    // Total Solids formula (example)
    fun calculateTS(fat: Double, lr: Double, volume: Double): Double {

        return if (fat == 0.0 && lr == 0.0) {
            0.0
        } else {
            val snf = 0.72 + (lr * 0.25) + (fat * 0.22)
            val ts = ((snf + fat) * volume) / 13.0
            return ts
        }


    }

    // Price formula using rate snapshot
    fun calculatePrice(volume: Double, fat: Double?, lr: Double?, rate: Double): Double {
        return if (fat == null || lr == null || (fat == 0.0 && lr == 0.0)) {
            // Flat price calculation when fat and lr are not provided
            volume * rate
        } else {
            val ts = calculateTS(fat, lr, volume)
            rate * ts
        }
    }


    fun calculateCustomerPrice(volume: Double, deduction: Double, rate: Double): Double {
        return (volume - deduction) * rate
    }
}

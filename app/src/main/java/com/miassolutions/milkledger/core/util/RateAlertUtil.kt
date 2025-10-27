package com.miassolutions.milkledger.core.util



import com.miassolutions.milkledger.presentation.supplier.supplierdetail.SupplierDetailModel

object RateChangeUtils {

    /**
     * Flags the start of rate changes in a list of SupplierDetailModel.
     * Works for any sorted list (ascending or descending).
     */
    fun flagRateChangeStarts(list: List<SupplierDetailModel>): List<SupplierDetailModel> {
        if (list.size <= 1) return list

        val mutableList = list.toMutableList()
        val isDescending = mutableList.first().date.isAfter(mutableList.last().date)

        if (!isDescending) {
            if (mutableList.first().isRateChanged)
                mutableList[0] = mutableList.first().copy(isRateChangeStart = true)

            for (i in 1 until mutableList.size) {
                val prev = mutableList[i - 1]
                val curr = mutableList[i]
                val rateChanged = curr.rateUsed != prev.rateUsed
                mutableList[i] = curr.copy(isRateChangeStart = rateChanged)
            }
        } else {
            if (mutableList.first().isRateChanged)
                mutableList[0] = mutableList.first().copy(isRateChangeStart = true)

            for (i in 0 until mutableList.size - 1) {
                val curr = mutableList[i]
                val next = mutableList[i + 1]
                val rateChanged = curr.rateUsed != next.rateUsed
                mutableList[i] = curr.copy(isRateChangeStart = rateChanged)
            }
        }

        return mutableList
    }
}

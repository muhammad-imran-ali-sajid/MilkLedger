package com.miassolutions.milkledger.utils.datefilter

interface DateFilterCallback {
    fun onPeriodChanged(period: DatePeriod)
}

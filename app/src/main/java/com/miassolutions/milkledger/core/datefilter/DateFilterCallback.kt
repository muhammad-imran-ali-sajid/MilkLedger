package com.miassolutions.milkledger.core.datefilter

interface DateFilterCallback {
    fun onPeriodChanged(period: DatePeriod)
}

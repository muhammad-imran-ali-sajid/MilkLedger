package com.miassolutions.milkledger.presentation.datefilter

interface DateFilterCallback {
    fun onPeriodChanged(period: DatePeriod)
}

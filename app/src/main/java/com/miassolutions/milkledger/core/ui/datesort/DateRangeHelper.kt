package com.miassolutions.milkledger.core.ui.datesort

import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

object DateRangeHelper {

    fun getRange(type: DateRangeType): Pair<LocalDate, LocalDate> {
        val today = LocalDate.now()
        return when (type) {
            DateRangeType.TODAY -> today to today
            DateRangeType.THIS_WEEK -> {
                val start = today.with(java.time.DayOfWeek.MONDAY)
                val end = today.with(java.time.DayOfWeek.SUNDAY)
                start to end
            }
            DateRangeType.THIS_MONTH -> {
                val start = today.with(TemporalAdjusters.firstDayOfMonth())
                val end = today.with(TemporalAdjusters.lastDayOfMonth())
                start to end
            }
            else -> LocalDate.MIN to LocalDate.MAX
        }
    }
}

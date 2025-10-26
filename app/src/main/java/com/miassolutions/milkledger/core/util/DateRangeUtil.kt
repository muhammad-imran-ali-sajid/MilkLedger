package com.miassolutions.milkledger.core.util

import java.time.LocalDate

object DateRangeUtil {
    fun thisWeek(): Pair<LocalDate, LocalDate> {
        val today = LocalDate.now()
        val start = today.minusDays(today.dayOfWeek.value.toLong() - 1)
        val end = start.plusDays(6)
        return start to end
    }

    fun thisMonth(): Pair<LocalDate, LocalDate> {
        val today = LocalDate.now()
        val start = today.withDayOfMonth(1)
        val end = today.withDayOfMonth(today.lengthOfMonth())
        return start to end
    }

    fun thisYear(): Pair<LocalDate, LocalDate> {
        val today = LocalDate.now()
        val start = today.withDayOfYear(1)
        val end = today.withDayOfYear(today.lengthOfYear())
        return start to end
    }
}

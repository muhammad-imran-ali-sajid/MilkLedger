package com.miassolutions.milkledger.core.util

import java.time.DayOfWeek
import java.time.LocalDate

object DateRangeHelper {
    fun today(): Pair<LocalDate, LocalDate> = LocalDate.now().let { it to it }

    fun thisWeek(): Pair<LocalDate, LocalDate> {
        val now = LocalDate.now()
        val start = now.with(DayOfWeek.MONDAY)
        val end = now.with(DayOfWeek.SUNDAY)
        return start to end
    }

    fun thisMonth(): Pair<LocalDate, LocalDate> {
        val now = LocalDate.now()
        val start = now.withDayOfMonth(1)
        val end = now.withDayOfMonth(now.lengthOfMonth())
        return start to end
    }
}

package com.miassolutions.datesort

import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import java.time.temporal.WeekFields
import java.util.*

object DateRangeHelper {

    fun getRange(type: DateRangeType, reference: LocalDate = LocalDate.now()): Pair<LocalDate, LocalDate> {
        return when (type) {
            DateRangeType.DAY -> reference to reference
            DateRangeType.WEEK -> {
                val weekFields = WeekFields.of(Locale.getDefault())
                val start = reference.with(weekFields.dayOfWeek(), 1)
                val end = start.plusDays(6)
                start to end
            }
            DateRangeType.MONTH -> {
                val start = reference.withDayOfMonth(1)
                val end = reference.with(TemporalAdjusters.lastDayOfMonth())
                start to end
            }
            DateRangeType.YEAR -> {
                val start = reference.withDayOfYear(1)
                val end = reference.with(TemporalAdjusters.lastDayOfYear())
                start to end
            }
            DateRangeType.CUSTOM -> reference to reference // handled separately
        }
    }

    fun next(type: DateRangeType, currentStart: LocalDate): Pair<LocalDate, LocalDate> {
        val ref = when (type) {
            DateRangeType.DAY -> currentStart.plusDays(1)
            DateRangeType.WEEK -> currentStart.plusWeeks(1)
            DateRangeType.MONTH -> currentStart.plusMonths(1)
            DateRangeType.YEAR -> currentStart.plusYears(1)
            DateRangeType.CUSTOM -> currentStart
        }
        return getRange(type, ref)
    }

    fun previous(type: DateRangeType, currentStart: LocalDate): Pair<LocalDate, LocalDate> {
        val ref = when (type) {
            DateRangeType.DAY -> currentStart.minusDays(1)
            DateRangeType.WEEK -> currentStart.minusWeeks(1)
            DateRangeType.MONTH -> currentStart.minusMonths(1)
            DateRangeType.YEAR -> currentStart.minusYears(1)
            DateRangeType.CUSTOM -> currentStart
        }
        return getRange(type, ref)
    }
}
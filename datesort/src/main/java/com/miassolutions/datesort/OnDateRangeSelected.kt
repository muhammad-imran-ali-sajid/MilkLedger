package com.miassolutions.datesort

import java.time.LocalDate

interface OnDateRangeSelected {
    fun onDateRangeSelected(start: LocalDate, end: LocalDate, type: DateRangeType)
}

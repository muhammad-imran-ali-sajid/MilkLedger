package com.miassolutions.milkledger.presentation.datefilter

import java.time.LocalDate
import java.time.YearMonth

sealed class DatePeriod {
    data class Daily(val date: LocalDate) : DatePeriod()
    data class Weekly(val start: LocalDate, val end: LocalDate) : DatePeriod()
    data class Monthly(val month: YearMonth) : DatePeriod()
    data class Yearly(val year: Int) : DatePeriod()
    data object All : DatePeriod()
    data class Custom(val start: LocalDate, val end: LocalDate) : DatePeriod()
}

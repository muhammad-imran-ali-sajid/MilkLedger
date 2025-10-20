package com.miassolutions.datesort

import java.time.LocalDate

data class DateFilterUiState(
    val type: DateRangeType = DateRangeType.DAY,
    val startDate: LocalDate = LocalDate.now(),
    val endDate: LocalDate = LocalDate.now(),
    val formattedRange: String = ""
)
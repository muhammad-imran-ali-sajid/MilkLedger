package com.miassolutions.datesort

import java.time.LocalDate

sealed class DateFilterUiEvent {
    data class OnTypeSelected(val type: DateRangeType) : DateFilterUiEvent()
    data object OnNextClicked : DateFilterUiEvent()
    data object OnPreviousClicked : DateFilterUiEvent()
    data class OnCustomRangeSelected(val start: LocalDate, val end: LocalDate) : DateFilterUiEvent()
}
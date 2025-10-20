package com.miassolutions.datesort

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class DateFilterViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(DateFilterUiState())
    val uiState = _uiState.asStateFlow()

    private val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy")

    init {
        updateRange(DateRangeType.DAY)
    }

    fun onEvent(event: DateFilterUiEvent) {
        when (event) {
            is DateFilterUiEvent.OnTypeSelected -> updateRange(event.type)
            DateFilterUiEvent.OnNextClicked -> navigate(next = true)
            DateFilterUiEvent.OnPreviousClicked -> navigate(next = false)
            is DateFilterUiEvent.OnCustomRangeSelected -> {
                _uiState.value = _uiState.value.copy(
                    type = DateRangeType.CUSTOM,
                    startDate = event.start,
                    endDate = event.end,
                    formattedRange = "${event.start.format(formatter)} - ${event.end.format(formatter)}"
                )
            }
        }
    }

    private fun updateRange(type: DateRangeType) {
        val (start, end) = DateRangeHelper.getRange(type)
        _uiState.value = _uiState.value.copy(
            type = type,
            startDate = start,
            endDate = end,
            formattedRange = "${start.format(formatter)} - ${end.format(formatter)}"
        )
    }

    private fun navigate(next: Boolean) {
        val state = _uiState.value
        val (start, end) = if (next)
            DateRangeHelper.next(state.type, state.startDate)
        else
            DateRangeHelper.previous(state.type, state.startDate)

        _uiState.value = state.copy(
            startDate = start,
            endDate = end,
            formattedRange = "${start.format(formatter)} - ${end.format(formatter)}"
        )
    }
}
package com.miassolutions.sort_filter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class DataSortViewModel(
    filters: List<FilterOption>,
    sorts: List<SortOption>
) : ViewModel() {

    private val _uiState = MutableStateFlow(DataSortUiState(filters, sorts))
    val uiState: StateFlow<DataSortUiState> = _uiState

    fun onEvent(event: DataSortUiEvent) {
        when (event) {
            is DataSortUiEvent.FilterChanged -> _uiState.update { state ->
                state.copy(
                    filterOptions = state.filterOptions.map {
                        if (it.id == event.id) it.copy(isSelected = !it.isSelected) else it
                    }
                )
            }

            is DataSortUiEvent.SortChanged -> _uiState.update { state ->
                state.copy(
                    sortOptions = state.sortOptions.map {
                        it.copy(isSelected = it.id == event.id)
                    }
                )
            }

            DataSortUiEvent.ResetClicked -> _uiState.update {
                it.copy(
                    filterOptions = it.filterOptions.map { f -> f.copy(isSelected = false) },
                    sortOptions = it.sortOptions.map { s -> s.copy(isSelected = false) }
                )
            }

            DataSortUiEvent.ApplyClicked -> Unit
        }
    }
}

@Suppress("UNCHECKED_CAST")
class DataSortViewModelFactory(
    private val filters: List<FilterOption>,
    private val sorts: List<SortOption>
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return DataSortViewModel(filters, sorts) as T
    }
}
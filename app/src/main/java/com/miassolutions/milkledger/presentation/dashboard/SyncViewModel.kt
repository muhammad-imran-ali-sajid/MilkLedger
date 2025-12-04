package com.miassolutions.milkledger.presentation.dashboard


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miassolutions.milkledger.data.repository.DataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SyncViewModel @Inject constructor(
    private val dataRepository: DataRepository
) : ViewModel() {

    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState: StateFlow<SyncState> = _syncState

    /**
     * Triggers the full synchronization process.
     */
    fun startInitialSync() {
        if (_syncState.value != SyncState.Loading) {
            _syncState.value = SyncState.Loading
            viewModelScope.launch {
                try {
                    dataRepository.setupRealtimeListeners()
                    _syncState.value = SyncState.Success
                } catch (e: Exception) {
                    _syncState.value = SyncState.Error("Initial sync failed: ${e.message}")
                }
            }
        }
    }
}

sealed class SyncState {
    data object Idle : SyncState()
    data object Loading : SyncState()
    data object Success : SyncState()
    data class Error(val message: String) : SyncState()
}
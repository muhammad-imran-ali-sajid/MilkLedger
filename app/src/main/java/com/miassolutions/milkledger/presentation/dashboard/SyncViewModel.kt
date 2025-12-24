package com.miassolutions.milkledger.presentation.dashboard


import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SyncViewModel @Inject constructor(

) : ViewModel() {


}


sealed class SyncState {
    data object Idle : SyncState()
    data object Loading : SyncState()
    data object Success : SyncState()
    data class Error(val message: String) : SyncState()
}
package com.miassolutions.milkledger.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miassolutions.milkledger.data.repository.CustomerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.launch


@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val customerRepository: CustomerRepository
) : ViewModel() {

    fun manualSync() {
        viewModelScope.launch {
            customerRepository.syncPendingToFirestore()
            customerRepository.refreshFromFirestore()
        }
    }
}

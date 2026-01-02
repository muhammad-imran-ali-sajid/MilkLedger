package com.miassolutions.milkledger.core.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

abstract class BaseViewModel<S, E, F>(initialState: S) : ViewModel() {

    private val _uiState = MutableStateFlow(initialState)
    val uiState = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<F>(
        replay = 0,
        extraBufferCapacity = 1
    )
    val uiEffect = _uiEffect.asSharedFlow()


    val currentState: S
        get() = _uiState.value

    abstract fun onEvent(event: E)

    protected fun updateState(reducer: (S) -> S) {
        _uiState.update(reducer)

    }

    protected fun emitEffect(effect: F) {
        _uiEffect.tryEmit(effect)
    }
}
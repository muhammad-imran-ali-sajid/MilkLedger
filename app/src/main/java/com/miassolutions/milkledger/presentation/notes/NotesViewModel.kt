package com.miassolutions.milkledger.presentation.notes


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miassolutions.milkledger.data.local.entities.NoteEntity
import com.miassolutions.milkledger.data.repository.NoteRepository

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotesViewModel @Inject constructor(
    private val repository: NoteRepository
) : ViewModel() {

    // --- UiState ---
    private val _uiState = MutableStateFlow(NoteUiState())
    val uiState: StateFlow<NoteUiState> = _uiState.asStateFlow()

    // --- UiEvents (One-time) ---
    private val _eventFlow = MutableSharedFlow<NoteUiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        getAllNotes()
    }

    private fun getAllNotes() {
        viewModelScope.launch {
            repository.getAllNotes()
                .onStart { _uiState.update { it.copy(isLoading = true) } }
                .catch { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
                .collect { notes ->
                    _uiState.update { it.copy(isLoading = false, notes = notes) }
                }
        }
    }

    fun addOrUpdateNote(note: NoteEntity) {
        viewModelScope.launch {
            try {
                repository.insertOrUpdate(note)
                _eventFlow.emit(NoteUiEvent.NoteSaved)
            } catch (e: Exception) {
                _eventFlow.emit(NoteUiEvent.ShowMessage("Failed to save note"))
            }
        }
    }

    fun deleteNote(note: NoteEntity) {
        viewModelScope.launch {
            try {
                repository.delete(note)
                _eventFlow.emit(NoteUiEvent.NoteDeleted(note))
            } catch (e: Exception) {
                _eventFlow.emit(NoteUiEvent.ShowMessage("Failed to delete note"))
            }
        }
    }

    fun toggleIsDone(noteId: String, isDone: Boolean) {
        viewModelScope.launch {
            try {
                repository.updateDoneState(noteId, isDone)
            } catch (e: Exception) {
                _eventFlow.emit(NoteUiEvent.ShowMessage("Failed to update status"))
            }
        }
    }
}

package com.miassolutions.milkledger.presentation.notes

import androidx.lifecycle.ViewModel
import com.miassolutions.milkledger.data.local.entities.NoteEntity
import com.miassolutions.milkledger.data.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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

    // Keep the full list for filtering
    private var allNotes: List<NoteEntity> = emptyList()

    init {
        getAllNotes()
    }

    private fun getAllNotes() {
//        viewModelScope.launch {
//            repository.getAllNotes()
//                .onStart { _uiState.update { it.copy(isLoading = true) } }
//                .catch { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
//                .collect { notes ->
//                    allNotes = notes
//                    val query = _uiState.value.searchQuery
//                    _uiState.update {
//                        it.copy(
//                            isLoading = false,
//                            notes = filterNotes(query, notes)
//                        )
//                    }
//                }
//        }
    }

    fun addOrUpdateNote(note: NoteEntity) {
//        viewModelScope.launch {
//            try {
//                repository.upsert(note)
//                _eventFlow.emit(NoteUiEvent.NoteSaved)
//            } catch (e: Exception) {
//                _eventFlow.emit(NoteUiEvent.ShowMessage("Failed to save note"))
//            }
//        }
    }

    fun deleteNote(note: NoteEntity) {
//        viewModelScope.launch {
//            try {
//                repository.delete(note)
//                _eventFlow.emit(NoteUiEvent.NoteDeleted(note))
//            } catch (e: Exception) {
//                _eventFlow.emit(NoteUiEvent.ShowMessage("Failed to delete note"))
//            }
//        }
    }


    // ✅ Corrected search handler
    fun onSearchQueryChanged(query: String) {
        _uiState.update { current ->
            current.copy(
                searchQuery = query,
                notes = filterNotes(query, allNotes)
            )
        }
    }

    // ✅ Helper function to filter notes
    private fun filterNotes(query: String, notes: List<NoteEntity>): List<NoteEntity> {
        if (query.isBlank()) return notes
        val q = query.lowercase()
        return notes.filter {
            it.title.lowercase().contains(q) ||
                    it.content.lowercase().contains(q)
        }
    }
}

package com.miassolutions.milkledger.features.note.ui.form

import com.miassolutions.milkledger.features.note.data.local.NoteEntity


data class NoteUiState(
    val notes: List<NoteEntity> = emptyList(),
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val error: String? = null
)


sealed class NoteUiEvent {
    data class ShowMessage(val message: String) : NoteUiEvent()
    data object AddNote : NoteUiEvent()
    data class EditNote(val noteEntity: NoteEntity) : NoteUiEvent()
    data object NoteSaved : NoteUiEvent()
    data class NoteDeleted(val noteEntity: NoteEntity) : NoteUiEvent()
}
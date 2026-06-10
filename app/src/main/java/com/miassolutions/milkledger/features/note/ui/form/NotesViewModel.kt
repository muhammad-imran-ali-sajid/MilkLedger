package com.miassolutions.milkledger.features.note.ui.form


import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.miassolutions.milkledger.core.ui.BaseViewModel
import com.miassolutions.milkledger.features.note.alarm.AlarmScheduler
import com.miassolutions.milkledger.core.localdb.note.NoteEntity
import com.miassolutions.milkledger.features.note.data.repository.NoteRepository
import com.miassolutions.milkledger.utils.extensions.toCompleteDateFormatWithTime
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AddEditNoteViewModel @Inject constructor(
    private val repository: NoteRepository,
    private val alarmScheduler: AlarmScheduler,
    savedStateHandle: SavedStateHandle
) : BaseViewModel<AddEditNoteUiState, AddEditNoteUiEvent, AddEditNoteUiEffect>(
    AddEditNoteUiState()
) {

    init {
        // Navigation Argument se ID check karein (Agar Edit mode hai)
        val noteId = savedStateHandle.get<String>("noteId")
        if (noteId != null) {
            loadNote(noteId)
        }
    }

    private fun loadNote(id: String) {
        viewModelScope.launch {
            updateState { it.copy(isLoading = true) }
            val note = repository.getNoteById(id)

            if (note != null) {
                updateState {
                    it.copy(
                        isLoading = false,
                        noteId = note.noteId,
                        title = note.title,
                        content = note.content,
                        alarmTimestamp = note.alarmAtMillis,
                        alarmDisplayString = formatAlarmTime(note.alarmAtMillis)
                    )
                }
            } else {
                updateState { it.copy(isLoading = false) }
                emitEffect(AddEditNoteUiEffect.ShowSnackbar("Note not found"))
            }
        }
    }

    override fun onEvent(event: AddEditNoteUiEvent) {
        when (event) {
            is AddEditNoteUiEvent.OnTitleChange -> {
                updateState { it.copy(title = event.text) }
            }

            is AddEditNoteUiEvent.OnContentChange -> {
                updateState { it.copy(content = event.text) }
            }

            // Alarm Logic
            AddEditNoteUiEvent.OnAlarmLayoutClick -> {
                emitEffect(AddEditNoteUiEffect.OpenDateTimePicker(currentState.alarmTimestamp))
            }

            is AddEditNoteUiEvent.OnAlarmSet -> {
                updateState {
                    it.copy(
                        alarmTimestamp = event.timestamp,
                        alarmDisplayString = formatAlarmTime(event.timestamp)
                    )
                }
            }

            AddEditNoteUiEvent.OnRemoveAlarm -> {
                updateState {
                    it.copy(
                        alarmTimestamp = null, alarmDisplayString = "Set alarm"
                    )
                }
            }

            // Save Logic
            AddEditNoteUiEvent.OnSaveClick -> saveNote()
        }
    }

    private fun saveNote() {
        val title = currentState.title.trim()
        val content = currentState.content.trim()

        // 1. Validation
        if (title.isBlank()) {
            emitEffect(AddEditNoteUiEffect.ShowSnackbar("Please enter a title"))
            return
        }

        viewModelScope.launch {
            updateState { it.copy(isLoading = true) }

            // 2. ID aur Date Logic
            val currentId = currentState.noteId
            val finalNoteId = currentId ?: UUID.randomUUID().toString()

            // 🔥 CRITICAL FIX:
            // Agar "Edit" kar rahy hain, to purani 'createdAt' date DB se lein.
            // Agar "New" hai, to abhi ka time lein.
            var finalCreatedAt = System.currentTimeMillis()

            if (currentId != null) {
                // Edit Mode: Purani date fetch karein taake overwrite na ho
                val existingNote = repository.getNoteById(currentId)
                if (existingNote != null) {
                    finalCreatedAt = existingNote.createdAtMillis
                }
            }

            // 3. Entity Creation
            val note = NoteEntity(
                noteId = finalNoteId,
                title = title,
                content = content,
                alarmAtMillis = currentState.alarmTimestamp,

                // Dates
                createdAtMillis = finalCreatedAt, // ✅ Fixed
                updatedAtMillis = System.currentTimeMillis(), // Abhi update hua

                isSynced = false // Sync logic k liye reset
            )

            // 4. DB Save
            repository.saveNote(note)

            // Pehle purana alarm cancel (sirf edit case me)
            if (currentState.noteId != null) {
                alarmScheduler.cancel(note)
            }

// Phir naya alarm schedule
            if (currentState.alarmTimestamp != null) {
                alarmScheduler.schedule(note)
            }


            // 6. Finish
            updateState { it.copy(isLoading = false) }
            emitEffect(AddEditNoteUiEffect.ShowSnackbar("Note saved successfully"))
            emitEffect(AddEditNoteUiEffect.NavigateBack)
        }
    }

    private fun formatAlarmTime(millis: Long?): String {
        return millis?.toCompleteDateFormatWithTime() ?: "Set alarm"
    }
}
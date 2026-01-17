package com.miassolutions.milkledger.features.note.ui.form


data class AddEditNoteUiState(
    val isLoading: Boolean = false,
    val noteId: String? = null, // Null = Create New, Not Null = Edit Mode
    val title: String = "",
    val content: String = "",
    val alarmTimestamp: Long? = null, // Null = No Alarm
    val alarmDisplayString: String = "Set alarm" // UI text
)

sealed class AddEditNoteUiEvent {
    // Input Fields
    data class OnTitleChange(val text: String) : AddEditNoteUiEvent()
    data class OnContentChange(val text: String) : AddEditNoteUiEvent()

    // Alarm Actions
    object OnAlarmLayoutClick : AddEditNoteUiEvent()
    data class OnAlarmSet(val timestamp: Long) : AddEditNoteUiEvent()
    object OnRemoveAlarm : AddEditNoteUiEvent()

    // Main Action
    object OnSaveClick : AddEditNoteUiEvent()
}

sealed class AddEditNoteUiEffect {
    object NavigateBack : AddEditNoteUiEffect()
    data class ShowSnackbar(val message: String) : AddEditNoteUiEffect()

    // Dialogs open karne k liye effects
    data class OpenDateTimePicker(val currentSelection: Long?) : AddEditNoteUiEffect()
}
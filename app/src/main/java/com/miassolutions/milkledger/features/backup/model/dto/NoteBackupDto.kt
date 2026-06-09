package com.miassolutions.milkledger.features.backup.model.dto

import kotlinx.serialization.Serializable

@Serializable
data class NoteBackupDto(
    val noteId: String,
    val title: String,
    val content: String,
    val alarmAtMillis: Long?,
    
    val createdAtMillis: Long,
    val updatedAtMillis: Long,
    val isSynced: Boolean,
    val deletedAtMillis: Long?
)
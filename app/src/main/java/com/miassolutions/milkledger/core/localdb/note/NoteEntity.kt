package com.miassolutions.milkledger.core.localdb.note

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "note_table")
data class NoteEntity(
    @PrimaryKey
    val noteId: String = UUID.randomUUID().toString(),
    val title: String,
    val content: String,
    val alarmAtMillis: Long? = null,

    val createdAtMillis: Long = System.currentTimeMillis(),
    val updatedAtMillis: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false,
    val deletedAtMillis: Long? = null
)
package com.miassolutions.milkledger.data.local.entities


import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "note_table")
data class NoteEntity(
    @PrimaryKey
    val noteId: String = UUID.randomUUID().toString(),

    val title: String,
    val content: String,
    val isDone: Boolean,

    val createdAtMillis: Long = System.currentTimeMillis(),
    val alarmAtMillis: Long? = null
)


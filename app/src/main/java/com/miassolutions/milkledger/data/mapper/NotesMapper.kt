package com.miassolutions.milkledger.data.mapper

import com.miassolutions.milkledger.data.local.entities.NoteEntity
import com.miassolutions.milkledger.data.remote.model.FirestoreNotes
import java.time.LocalDateTime

fun NoteEntity.toFirestoreModel(): FirestoreNotes {
    return FirestoreNotes(
        id = id,
        title = title,
        content = content,
        createdDate = createdDate.toString(),               // ensure String
        alarmDate = alarmDateTime?.toString() ?: LocalDateTime.now().toString(),        // safe null check
        isDone = isDone
    )
}

fun FirestoreNotes.toEntity(): NoteEntity {
    return NoteEntity(
        id = id,
        title = title,
        content = content,
        createdDate = LocalDateTime.parse(createdDate),     // parse from string
        alarmDateTime = alarmDate.takeIf { it.isNotBlank() }?.let { LocalDateTime.parse(it) },
        isDone = isDone
    )
}

package com.miassolutions.milkledger.features.note.data.mapper

import com.miassolutions.milkledger.core.localdb.note.NoteEntity
import com.miassolutions.milkledger.features.note.domain.Note
import com.miassolutions.milkledger.utils.extensions.toLocalDateTime
import com.miassolutions.milkledger.utils.extensions.toMillis

fun NoteEntity.toDomain(): Note =
    Note(
        id = noteId,
        title = title,
        content = content,
        createdAt = createdAtMillis.toLocalDateTime(),
        alarmAt = alarmAtMillis?.toLocalDateTime(),
    )

fun Note.toEntity(): NoteEntity =
    NoteEntity(
        noteId = id,
        title = title,
        content = content,
        createdAtMillis = createdAt.toMillis(),
        alarmAtMillis = alarmAt?.toMillis()
    )

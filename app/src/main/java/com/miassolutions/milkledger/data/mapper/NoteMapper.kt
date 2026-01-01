package com.miassolutions.milkledger.data.mapper

import com.miassolutions.milkledger.utils.extensions.toLocalDateTime
import com.miassolutions.milkledger.utils.extensions.toMillis
import com.miassolutions.milkledger.data.local.entities.NoteEntity
import com.miassolutions.milkledger.domain.model.Note

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

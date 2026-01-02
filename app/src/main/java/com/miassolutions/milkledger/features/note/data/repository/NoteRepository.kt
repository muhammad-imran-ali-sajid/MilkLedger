package com.miassolutions.milkledger.features.note.data.repository

import com.miassolutions.milkledger.features.note.data.local.NoteDao
import com.miassolutions.milkledger.features.note.data.local.NoteEntity
import com.miassolutions.milkledger.features.note.data.mapper.toDomain
import com.miassolutions.milkledger.features.note.data.mapper.toEntity
import com.miassolutions.milkledger.features.note.domain.Note
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteRepository @Inject constructor(
    private val noteDao: NoteDao,
) {



    fun getAllNotes(): Flow<List<Note>> =
        noteDao.getAllNotes().map { it.map(NoteEntity::toDomain) }



    suspend fun getNoteById(id: String): Note? =
        noteDao.getNoteById(id)?.toDomain()



    suspend fun upsert(note: Note) {
        val entity = note.toEntity()
        noteDao.upsert(entity)


    }



    suspend fun delete(noteId: String) {
        noteDao.deleteById(noteId)

    }



}
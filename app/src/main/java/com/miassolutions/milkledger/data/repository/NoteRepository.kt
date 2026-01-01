package com.miassolutions.milkledger.data.repository

import com.miassolutions.milkledger.data.local.daos.NoteDao
import com.miassolutions.milkledger.data.local.entities.NoteEntity
import com.miassolutions.milkledger.data.mapper.toDomain
import com.miassolutions.milkledger.data.mapper.toEntity
import com.miassolutions.milkledger.domain.model.Note
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

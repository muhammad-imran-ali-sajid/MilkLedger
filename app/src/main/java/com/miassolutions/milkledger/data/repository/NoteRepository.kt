package com.miassolutions.milkledger.data.repository


import com.miassolutions.milkledger.data.local.daos.NoteDao
import com.miassolutions.milkledger.data.local.entities.NoteEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteRepository @Inject constructor(
    private val noteDao: NoteDao
) {

    fun getAllNotes(): Flow<List<NoteEntity>> = noteDao.getAllNotes()

    fun getDoneNotes(): Flow<List<NoteEntity>> = noteDao.getDoneNotes()

    fun getPendingNotes(): Flow<List<NoteEntity>> = noteDao.getPendingNotes()

    suspend fun getNoteById(noteId: String): NoteEntity? = noteDao.getNoteById(noteId)

    suspend fun insertOrUpdate(note: NoteEntity) = noteDao.insertOrUpdate(note)

    suspend fun delete(note: NoteEntity) = noteDao.delete(note)

    suspend fun deleteAllNotes() = noteDao.deleteAllNotes()

    suspend fun updateDoneState(noteId: String, isDone: Boolean) =
        noteDao.updateDoneState(noteId, isDone)
}

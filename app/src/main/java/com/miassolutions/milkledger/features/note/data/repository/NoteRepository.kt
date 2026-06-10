package com.miassolutions.milkledger.features.note.data.repository

import com.miassolutions.milkledger.features.backup.data.BackupRepository
import com.miassolutions.milkledger.features.note.data.local.NoteDao
import com.miassolutions.milkledger.features.note.data.local.NoteEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class NoteRepository @Inject constructor(
    private val dao: NoteDao,
    private val backupRepository: BackupRepository
) {

    fun getAllNotes(): Flow<List<NoteEntity>> = dao.getAllNotes()

    suspend fun getNoteById(id: String): NoteEntity? = dao.getNoteById(id)

    suspend fun saveNote(note: NoteEntity) {
        // Agar sync logic hai to isSynced = false rakhein taake baad me upload ho
        dao.insertNote(note.copy(isSynced = false, updatedAtMillis = System.currentTimeMillis()))
        backupRepository.markDataChanged()
    }

    suspend fun deleteNote(id: String) {
        dao.softDeleteNote(id)
        backupRepository.markDataChanged()
    }
}
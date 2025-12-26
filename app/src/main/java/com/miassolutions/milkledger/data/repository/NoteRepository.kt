package com.miassolutions.milkledger.data.repository

import android.util.Log
import com.miassolutions.milkledger.data.local.daos.NoteDao
import com.miassolutions.milkledger.data.local.entities.NoteEntity
import com.miassolutions.milkledger.data.mapper.toDomain
import com.miassolutions.milkledger.data.mapper.toEntity
import com.miassolutions.milkledger.presentation.expenses.data.toDomain
import com.miassolutions.milkledger.presentation.expenses.data.toEntity
import com.miassolutions.milkledger.data.remote.FirestoreSyncHelper
import com.miassolutions.milkledger.domain.model.Note
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteRepository @Inject constructor(
    private val noteDao: NoteDao,
    private val firestore: FirestoreSyncHelper
) {

    companion object {
        private const val COLLECTION = "notes"
        private const val TAG = "NoteRepository"
    }

    // -------------------------
    // READ
    // -------------------------

    fun getAllNotes(): Flow<List<Note>> =
        noteDao.getAllNotes().map { it.map(NoteEntity::toDomain) }

    fun getPendingNotes(): Flow<List<Note>> =
        noteDao.getPendingNotes().map { it.map(NoteEntity::toDomain) }

    fun getDoneNotes(): Flow<List<Note>> =
        noteDao.getDoneNotes().map { it.map(NoteEntity::toDomain) }

    suspend fun getNoteById(id: String): Note? =
        noteDao.getNoteById(id)?.toDomain()

    // -------------------------
    // WRITE
    // -------------------------

    suspend fun upsert(note: Note) {
        val entity = note.toEntity()
        noteDao.upsert(entity)

//        syncSafely {
//            firestore.uploadSingle(
//                COLLECTION,
//                entity.id,
//                entity.toFirestoreModel()
//            )
//        }
    }

    suspend fun updateDoneState(noteId: String, isDone: Boolean) {
        noteDao.updateDoneState(noteId, isDone)

//        noteDao.getNoteById(noteId)?.let { entity ->
//            syncSafely {
//                firestore.uploadSingle(
//                    COLLECTION,
//                    entity.noteId,
//                    entity.toFirestoreModel()
//                )
//            }
//        }
    }

    suspend fun delete(noteId: String) {
        noteDao.deleteById(noteId)

        syncSafely {
            firestore.deleteDocument(COLLECTION, noteId)
        }
    }

    // -------------------------
    // SYNC
    // -------------------------

//    suspend fun restoreAllNotes() {
//        try {
//            firestore.downloadCollection<NoteEntity>(COLLECTION)
//                .takeIf { it.isNotEmpty() }
//                ?.let(noteDao::upsertAll)
//        } catch (e: Exception) {
//            Log.e(TAG, "Restore failed", e)
//        }
//    }

    // -------------------------
    // UTILS
    // -------------------------

    private suspend fun syncSafely(block: suspend () -> Unit) {
        try { block() }
        catch (e: Exception) {
            Log.e(TAG, "Firestore sync failed", e)
        }
    }
}

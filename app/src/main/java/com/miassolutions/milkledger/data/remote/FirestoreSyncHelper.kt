package com.miassolutions.milkledger.data.remote

import android.util.Log
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.min

@Singleton
class FirestoreSyncHelper @Inject constructor(
    val firestore: FirebaseFirestore
) {

    companion object {
        const val TAG = "FirestoreSyncHelper"
        private const val FIRESTORE_BATCH_LIMIT = 500
    }

    /**
     * Upload a collection of entities to Firestore.
     */
    suspend fun <T : Any> uploadCollection(
        collectionName: String,
        dataList: List<T>,
        idExtractor: ((T) -> String?)? = null
    ) {
        if (!SyncConfig.ENABLE_FIRESTORE_SYNC) {
            Log.d(TAG, "🔌 Firestore sync DISABLED → uploadCollection skipped ($collectionName)")
            return
        }

        if (dataList.isEmpty()) {
            Log.d(TAG, "uploadCollection: no items to upload for $collectionName")
            return
        }

        try {
            val collectionRef = firestore.collection(collectionName)

            // chunk into batches of 500
            var index = 0
            while (index < dataList.size) {
                val end = min(index + FIRESTORE_BATCH_LIMIT, dataList.size)
                val subList = dataList.subList(index, end)
                val batch = firestore.batch()

                for (item in subList) {
                    val docId = idExtractor?.invoke(item) ?: fallbackDocumentId(item)
                    val docRef: DocumentReference = if (!docId.isNullOrBlank()) {
                        collectionRef.document(docId)
                    } else {
                        collectionRef.document() // auto id
                    }
                    batch.set(docRef, item, SetOptions.merge())
                }

                batch.commit().await()
                index = end
            }

            Log.d(TAG, "Uploaded ${dataList.size} items to $collectionName")
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading $collectionName: ${e.localizedMessage}", e)
            throw e
        }
    }

    /**
     * Upload a single document (insert or update).
     */
    suspend fun <T : Any> uploadSingle(
        collectionName: String,
        documentId: String?,
        data: T
    ) {
        if (!SyncConfig.ENABLE_FIRESTORE_SYNC) {
            Log.d(TAG, "🔌 Firestore sync DISABLED → uploadSingle skipped ($collectionName)")
            return
        }

        try {
            val docRef = if (!documentId.isNullOrBlank()) {
                firestore.collection(collectionName).document(documentId)
            } else {
                firestore.collection(collectionName).document()
            }
            docRef.set(data, SetOptions.merge()).await()
            Log.d(TAG, "Uploaded $collectionName/${docRef.id} successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading single doc: ${e.localizedMessage}", e)
            throw e
        }
    }

    /**
     * Delete a single document.
     */
    suspend fun deleteDocument(
        collectionName: String,
        documentId: String
    ) {
        if (!SyncConfig.ENABLE_FIRESTORE_SYNC) {
            Log.d(TAG, "🔌 Firestore sync DISABLED → deleteDocument skipped ($collectionName/$documentId)")
            return
        }

        try {
            firestore.collection(collectionName).document(documentId).delete().await()
            Log.d(TAG, "Deleted $collectionName/$documentId successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting doc $documentId in $collectionName: ${e.localizedMessage}", e)
            throw e
        }
    }

    /**
     * Download a collection into POJOs.
     */
    suspend inline fun <reified T> downloadCollection(
        collectionName: String
    ): List<T> {
        if (!SyncConfig.ENABLE_FIRESTORE_SYNC) {
            Log.d(TAG, "🔌 Firestore sync DISABLED → downloadCollection returns EMPTY ($collectionName)")
            return emptyList()
        }

        return try {
            val snapshot = firestore.collection(collectionName).get().await()
            snapshot.toObjects(T::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Error downloading $collectionName: ${e.localizedMessage}", e)
            emptyList()
        }
    }

    /**
     * Fallback document id extraction using reflection.
     */
    private fun fallbackDocumentId(item: Any): String? {
        val clazz = item::class
        val candidateNames = listOf(
            "id", "Id", "ID",
            "customerId", "customer_id",
            "noteId", "entityId", "uid"
        )
        for (name in candidateNames) {
            try {
                val prop = clazz.members.firstOrNull { it.name.equals(name, ignoreCase = true) }
                val value = prop?.call(item)?.toString()
                if (!value.isNullOrBlank()) return value
            } catch (_: Exception) { }
        }
        return null
    }
}

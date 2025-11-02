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
     *
     * @param collectionName Firestore collection name
     * @param dataList list of entities (must be non-nullable)
     * @param idExtractor optional lambda to get the document id from entity. If null a reflection fallback will try common id field names.
     */
    suspend fun <T : Any> uploadCollection(
        collectionName: String,
        dataList: List<T>,
        idExtractor: ((T) -> String?)? = null
    ) {
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
                    // set uses the raw object; T : Any ensures it's allowed
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
     * Download a collection into POJOs.
     */
    suspend inline fun <reified T> downloadCollection(
        collectionName: String
    ): List<T> {
        return try {
            val snapshot = firestore.collection(collectionName).get().await()
            snapshot.toObjects(T::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Error downloading $collectionName: ${e.localizedMessage}", e)
            emptyList()
        }
    }

    /**
     * Fallback document id extraction using common field names via reflection.
     */
    private fun fallbackDocumentId(item: Any): String? {
        val clazz = item::class
        val candidateNames = listOf("id", "Id", "ID", "customerId", "customer_id", "noteId", "entityId", "uid")
        for (name in candidateNames) {
            try {
                // attempt property getter
                val prop = clazz.members.firstOrNull { it.name.equals(name, ignoreCase = true) }
                val value = prop?.call(item)?.toString()
                if (!value.isNullOrBlank()) return value
            } catch (_: Exception) {
                // ignore and continue
            }
        }
        return null
    }
}

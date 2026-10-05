package com.shadowings.devfestmilano26.apod.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.shadowings.devfestmilano26.apod.data.local.FavoriteEntity
import com.shadowings.devfestmilano26.apod.domain.model.SyncStatus
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Reads/writes against `users/{uid}/favorites/{date}` per contracts/firestore-schema.md. The
 * document shape mirrors [FavoriteEntity] minus `date` (the document id), `syncStatus`, and
 * `ownerUid` (both implicit -- every document here is, by definition, that user's SYNCED
 * favorite).
 */
@Singleton
class FirestoreFavoritesSource @Inject constructor(
    private val firestore: FirebaseFirestore,
) {
    private fun collection(uid: String) =
        firestore.collection("users").document(uid).collection("favorites")

    suspend fun upsert(uid: String, entity: FavoriteEntity) {
        val document = mapOf(
            "title" to entity.title,
            "explanation" to entity.explanation,
            "url" to entity.url,
            "hdUrl" to entity.hdUrl,
            "mediaType" to entity.mediaType,
            "copyright" to entity.copyright,
            "savedAtEpochMillis" to entity.savedAtEpochMillis,
        )
        collection(uid).document(entity.date).set(document).await()
    }

    suspend fun delete(uid: String, date: String) {
        collection(uid).document(date).delete().await()
    }

    /** One-shot full fetch, used by the Favorites screen's manual "Sync Now" action. */
    suspend fun getAll(uid: String): List<FavoriteEntity> {
        val snapshot = collection(uid).get().await()
        return snapshot.documents.map { document ->
            FavoriteEntity(
                date = document.id,
                title = document.getString("title") ?: "",
                explanation = document.getString("explanation") ?: "",
                url = document.getString("url") ?: "",
                hdUrl = document.getString("hdUrl"),
                mediaType = document.getString("mediaType") ?: "IMAGE",
                copyright = document.getString("copyright"),
                syncStatus = SyncStatus.SYNCED.name,
                ownerUid = uid,
                savedAtEpochMillis = document.getLong("savedAtEpochMillis") ?: 0L,
            )
        }
    }

    /** Remote -> local sync (research.md "Sync"): invoked once per snapshot change. */
    fun listen(
        uid: String,
        onChange: (added: List<FavoriteEntity>, removedDates: List<String>) -> Unit,
    ): ListenerRegistration = collection(uid).addSnapshotListener { snapshot, _ ->
        if (snapshot == null) return@addSnapshotListener
        val added = mutableListOf<FavoriteEntity>()
        val removed = mutableListOf<String>()
        for (change in snapshot.documentChanges) {
            val date = change.document.id
            when (change.type.name) {
                "REMOVED" -> removed += date
                else -> {
                    val data = change.document.data
                    added += FavoriteEntity(
                        date = date,
                        title = data["title"] as? String ?: "",
                        explanation = data["explanation"] as? String ?: "",
                        url = data["url"] as? String ?: "",
                        hdUrl = data["hdUrl"] as? String,
                        mediaType = data["mediaType"] as? String ?: "IMAGE",
                        copyright = data["copyright"] as? String,
                        syncStatus = SyncStatus.SYNCED.name,
                        ownerUid = uid,
                        savedAtEpochMillis = (data["savedAtEpochMillis"] as? Long) ?: 0L,
                    )
                }
            }
        }
        onChange(added, removed)
    }
}

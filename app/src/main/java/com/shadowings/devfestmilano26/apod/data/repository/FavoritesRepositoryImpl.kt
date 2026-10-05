package com.shadowings.devfestmilano26.apod.data.repository

import com.google.firebase.firestore.ListenerRegistration
import com.shadowings.devfestmilano26.apod.data.local.FavoriteDao
import com.shadowings.devfestmilano26.apod.data.local.FavoriteEntity
import com.shadowings.devfestmilano26.apod.data.local.SessionPreferences
import com.shadowings.devfestmilano26.apod.data.remote.FirestoreFavoritesSource
import com.shadowings.devfestmilano26.apod.domain.model.AstronomyPicture
import com.shadowings.devfestmilano26.apod.domain.model.Favorite
import com.shadowings.devfestmilano26.apod.domain.model.MediaType
import com.shadowings.devfestmilano26.apod.domain.model.SyncStatus
import com.shadowings.devfestmilano26.apod.domain.repository.FavoritesRepository
import com.shadowings.devfestmilano26.core.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Room ([FavoriteDao]) is always the source of truth the UI reads from. Firestore is mirrored to
 * only when the affected row is SYNCED (research.md "Sync"). A snapshot listener is
 * registered/unregistered as the session signs in/out, applying remote changes into Room.
 */
@Singleton
class FavoritesRepositoryImpl @Inject constructor(
    private val favoriteDao: FavoriteDao,
    private val firestoreSource: FirestoreFavoritesSource,
    private val sessionPreferences: SessionPreferences,
    @ApplicationScope private val applicationScope: CoroutineScope,
) : FavoritesRepository {

    private var remoteListener: ListenerRegistration? = null

    init {
        sessionPreferences.observe()
            .map { it.uid }
            .distinctUntilChanged()
            .onEach { uid -> updateRemoteListener(uid) }
            .launchIn(applicationScope)
    }

    private fun updateRemoteListener(uid: String?) {
        remoteListener?.remove()
        remoteListener = uid?.let { signedInUid ->
            firestoreSource.listen(signedInUid) { added, removedDates ->
                applicationScope.launch {
                    added.forEach { favoriteDao.upsert(it) }
                    removedDates.forEach { favoriteDao.deleteByDate(it) }
                }
            }
        }
    }

    override fun observeFavorites(): Flow<List<Favorite>> =
        favoriteDao.observeAll().map { list -> list.map { it.toDomain() } }

    override fun observeFavoriteStatus(date: LocalDate): Flow<SyncStatus?> =
        favoriteDao.observeStatus(date.toString()).map { it?.let(SyncStatus::valueOf) }

    override suspend fun saveFavorite(picture: AstronomyPicture): Result<Unit> = runCatching {
        val session = sessionPreferences.observe().first()
        val entity = picture.toFavoriteEntity(
            syncStatus = if (session.isSignedIn) SyncStatus.SYNCED else SyncStatus.LOCAL_ONLY,
            ownerUid = session.uid,
        )
        favoriteDao.upsert(entity)
        if (session.isSignedIn && session.uid != null) {
            runCatching { firestoreSource.upsert(session.uid, entity) }
        }
    }

    override suspend fun removeFavorite(date: LocalDate): Result<Unit> = runCatching {
        val dateKey = date.toString()
        val existing = favoriteDao.getByDate(dateKey)
        favoriteDao.deleteByDate(dateKey)
        if (existing?.syncStatus == SyncStatus.SYNCED.name && existing.ownerUid != null) {
            runCatching { firestoreSource.delete(existing.ownerUid, dateKey) }
        }
    }

    override suspend fun migrateLocalFavoritesToAccount(uid: String): Result<Unit> = runCatching {
        val localOnly = favoriteDao.getLocalOnly()
        for (entity in localOnly) {
            favoriteDao.updateSyncStatus(entity.date, SyncStatus.SYNCED.name, uid)
            runCatching {
                firestoreSource.upsert(uid, entity.copy(syncStatus = SyncStatus.SYNCED.name, ownerUid = uid))
            }
        }
    }

    override suspend fun syncNow(): Result<Unit> = runCatching {
        val session = sessionPreferences.observe().first()
        val uid = session.uid ?: return@runCatching
        val remote = firestoreSource.getAll(uid)
        for (entity in remote) {
            favoriteDao.upsert(entity)
        }
    }
}

private fun AstronomyPicture.toFavoriteEntity(syncStatus: SyncStatus, ownerUid: String?) =
    FavoriteEntity(
        date = date.toString(),
        title = title,
        explanation = explanation,
        url = url,
        hdUrl = hdUrl,
        mediaType = mediaType.name,
        copyright = copyright,
        syncStatus = syncStatus.name,
        ownerUid = ownerUid,
        savedAtEpochMillis = System.currentTimeMillis(),
    )

private fun FavoriteEntity.toDomain() = Favorite(
    picture = AstronomyPicture(
        date = LocalDate.parse(date),
        title = title,
        explanation = explanation,
        url = url,
        hdUrl = hdUrl,
        mediaType = MediaType.valueOf(mediaType),
        copyright = copyright,
    ),
    syncStatus = SyncStatus.valueOf(syncStatus),
    ownerUid = ownerUid,
    savedAtEpochMillis = savedAtEpochMillis,
)

package com.shadowings.devfestmilano26.apod.domain.usecase

import com.shadowings.devfestmilano26.apod.domain.model.AstronomyPicture
import com.shadowings.devfestmilano26.apod.domain.model.Favorite
import com.shadowings.devfestmilano26.apod.domain.model.SyncStatus
import com.shadowings.devfestmilano26.apod.domain.repository.FavoritesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

/** Shared in-memory fake used by the favorites-related use case tests. */
class FakeFavoritesRepository : FavoritesRepository {
    private val favorites = MutableStateFlow<Map<LocalDate, Favorite>>(emptyMap())

    override fun observeFavorites(): Flow<List<Favorite>> = favorites.map { it.values.toList() }

    override fun observeFavoriteStatus(date: LocalDate): Flow<SyncStatus?> =
        favorites.map { it[date]?.syncStatus }

    override suspend fun saveFavorite(picture: AstronomyPicture): Result<Unit> {
        favorites.value = favorites.value + (picture.date to Favorite(
            picture = picture,
            syncStatus = SyncStatus.LOCAL_ONLY,
            ownerUid = null,
            savedAtEpochMillis = 0L,
        ))
        return Result.success(Unit)
    }

    override suspend fun removeFavorite(date: LocalDate): Result<Unit> {
        favorites.value = favorites.value - date
        return Result.success(Unit)
    }

    override suspend fun migrateLocalFavoritesToAccount(uid: String): Result<Unit> {
        favorites.value = favorites.value.mapValues { (_, favorite) ->
            favorite.copy(syncStatus = SyncStatus.SYNCED, ownerUid = uid)
        }
        return Result.success(Unit)
    }

    override suspend fun syncNow(): Result<Unit> = Result.success(Unit)
}

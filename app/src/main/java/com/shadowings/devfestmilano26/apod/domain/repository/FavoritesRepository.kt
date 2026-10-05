package com.shadowings.devfestmilano26.apod.domain.repository

import com.shadowings.devfestmilano26.apod.domain.model.AstronomyPicture
import com.shadowings.devfestmilano26.apod.domain.model.SyncStatus
import com.shadowings.devfestmilano26.apod.domain.model.Favorite
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface FavoritesRepository {
    fun observeFavorites(): Flow<List<Favorite>>
    fun observeFavoriteStatus(date: LocalDate): Flow<SyncStatus?>
    suspend fun saveFavorite(picture: AstronomyPicture): Result<Unit>
    suspend fun removeFavorite(date: LocalDate): Result<Unit>
    suspend fun migrateLocalFavoritesToAccount(uid: String): Result<Unit>
    suspend fun syncNow(): Result<Unit>
}

package com.shadowings.devfestmilano26.apod.domain.usecase

import com.shadowings.devfestmilano26.apod.domain.repository.FavoritesRepository
import javax.inject.Inject

/** Invoked right after a successful sign-in to migrate LOCAL_ONLY favorites to SYNCED (FR-015). */
class SyncFavoritesOnLoginUseCase @Inject constructor(
    private val repository: FavoritesRepository,
) {
    suspend operator fun invoke(uid: String): Result<Unit> =
        repository.migrateLocalFavoritesToAccount(uid)
}

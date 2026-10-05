package com.shadowings.devfestmilano26.apod.domain.usecase

import com.shadowings.devfestmilano26.apod.domain.model.AstronomyPicture
import com.shadowings.devfestmilano26.apod.domain.repository.FavoritesRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/** Saves when not yet favorited, removes when already favorited (toggle semantics). */
class ToggleFavoriteUseCase @Inject constructor(
    private val repository: FavoritesRepository,
) {
    suspend operator fun invoke(picture: AstronomyPicture): Result<Unit> {
        val currentStatus = repository.observeFavoriteStatus(picture.date).first()
        return if (currentStatus == null) {
            repository.saveFavorite(picture)
        } else {
            repository.removeFavorite(picture.date)
        }
    }
}

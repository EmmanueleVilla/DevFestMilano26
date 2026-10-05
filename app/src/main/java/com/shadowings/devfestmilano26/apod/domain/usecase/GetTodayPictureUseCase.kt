package com.shadowings.devfestmilano26.apod.domain.usecase

import com.shadowings.devfestmilano26.apod.domain.model.AstronomyPicture
import com.shadowings.devfestmilano26.apod.domain.repository.TodayPictureRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Fetches today's picture; on failure, falls back to the cached entry. Surfaces failure only
 * when neither a fresh fetch nor a cache is available.
 */
class GetTodayPictureUseCase @Inject constructor(
    private val repository: TodayPictureRepository,
) {
    suspend operator fun invoke(): Result<AstronomyPicture> {
        val fetched = repository.getTodayPicture()
        if (fetched.isSuccess) return fetched

        val cached = repository.observeCachedTodayPicture().first()
        return if (cached != null) Result.success(cached) else fetched
    }
}

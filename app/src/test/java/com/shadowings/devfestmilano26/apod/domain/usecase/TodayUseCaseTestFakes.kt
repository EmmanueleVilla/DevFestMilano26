package com.shadowings.devfestmilano26.apod.domain.usecase

import com.shadowings.devfestmilano26.apod.domain.model.AstronomyPicture
import com.shadowings.devfestmilano26.apod.domain.repository.TodayPictureRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/** Shared fake used by Today-related use case tests. */
class FakeTodayPictureRepository(
    private val fetchResult: Result<AstronomyPicture>,
    private val cached: AstronomyPicture? = null,
) : TodayPictureRepository {
    override suspend fun getTodayPicture(): Result<AstronomyPicture> = fetchResult
    override fun observeCachedTodayPicture(): Flow<AstronomyPicture?> = flowOf(cached)
}

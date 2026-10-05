package com.shadowings.devfestmilano26.apod.domain.repository

import com.shadowings.devfestmilano26.apod.domain.model.AstronomyPicture
import kotlinx.coroutines.flow.Flow

interface TodayPictureRepository {
    suspend fun getTodayPicture(): Result<AstronomyPicture>
    fun observeCachedTodayPicture(): Flow<AstronomyPicture?>
}

package com.shadowings.devfestmilano26.apod.data.repository

import com.shadowings.devfestmilano26.apod.data.local.CachedTodayDao
import com.shadowings.devfestmilano26.apod.data.local.CachedTodayEntity
import com.shadowings.devfestmilano26.apod.data.remote.ApodApiService
import com.shadowings.devfestmilano26.apod.data.remote.toDomain
import com.shadowings.devfestmilano26.apod.domain.model.AstronomyPicture
import com.shadowings.devfestmilano26.apod.domain.model.MediaType
import com.shadowings.devfestmilano26.apod.domain.repository.TodayPictureRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class TodayPictureRepositoryImpl @Inject constructor(
    private val apiService: ApodApiService,
    private val cachedTodayDao: CachedTodayDao,
    @Named("nasaApodApiKey") private val apiKey: String,
) : TodayPictureRepository {

    override suspend fun getTodayPicture(): Result<AstronomyPicture> = runCatching {
        val picture = apiService.getAstronomyPictureOfTheDay(apiKey = apiKey).toDomain()
        cachedTodayDao.upsert(picture.toCachedEntity())
        picture
    }

    override fun observeCachedTodayPicture(): Flow<AstronomyPicture?> =
        cachedTodayDao.observe().map { it?.toDomain() }
}

private fun AstronomyPicture.toCachedEntity() = CachedTodayEntity(
    date = date.toString(),
    title = title,
    explanation = explanation,
    url = url,
    hdUrl = hdUrl,
    mediaType = mediaType.name,
    copyright = copyright,
    fetchedAtEpochMillis = System.currentTimeMillis(),
)

private fun CachedTodayEntity.toDomain() = AstronomyPicture(
    date = LocalDate.parse(date),
    title = title,
    explanation = explanation,
    url = url,
    hdUrl = hdUrl,
    mediaType = MediaType.valueOf(mediaType),
    copyright = copyright,
)

package com.shadowings.devfestmilano26.apod.data.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.shadowings.devfestmilano26.apod.data.local.AppDatabase
import com.shadowings.devfestmilano26.apod.data.local.CachedTodayDao
import com.shadowings.devfestmilano26.apod.data.remote.ApodApiService
import com.shadowings.devfestmilano26.apod.data.remote.ApodDto
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class TodayPictureRepositoryImplTest {

    private lateinit var db: AppDatabase
    private lateinit var cachedTodayDao: CachedTodayDao
    private lateinit var apiService: ApodApiService
    private lateinit var repository: TodayPictureRepositoryImpl

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java,
        ).build()
        cachedTodayDao = db.cachedTodayDao()
        apiService = mockk()
        repository = TodayPictureRepositoryImpl(apiService, cachedTodayDao, apiKey = "DEMO_KEY")
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `a successful fetch is cached locally`() = runTest {
        coEvery { apiService.getAstronomyPictureOfTheDay(any(), any()) } returns ApodDto(
            date = "2026-05-14",
            title = "The Star-Forming Pillars of M16",
            explanation = "Dense cold molecular hydrogen pillars.",
            url = "https://apod.example/m16.jpg",
            hdurl = "https://apod.example/m16-hd.jpg",
            mediaType = "image",
            copyright = null,
        )

        val result = repository.getTodayPicture()

        assertTrue(result.isSuccess)
        val cached = repository.observeCachedTodayPicture().first()
        assertEquals("The Star-Forming Pillars of M16", cached?.title)
    }

    @Test
    fun `a failed fetch surfaces failure without touching the cache`() = runTest {
        coEvery { apiService.getAstronomyPictureOfTheDay(any(), any()) } throws RuntimeException("network down")

        val result = repository.getTodayPicture()

        assertTrue(result.isFailure)
        assertEquals(null, repository.observeCachedTodayPicture().first())
    }
}

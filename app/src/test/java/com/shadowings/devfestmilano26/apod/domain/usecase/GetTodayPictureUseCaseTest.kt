package com.shadowings.devfestmilano26.apod.domain.usecase

import com.shadowings.devfestmilano26.apod.domain.model.AstronomyPicture
import com.shadowings.devfestmilano26.apod.domain.model.MediaType
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

private val samplePicture = AstronomyPicture(
    date = LocalDate.of(2026, 5, 14),
    title = "The Star-Forming Pillars of M16",
    explanation = "Dense cold molecular hydrogen pillars.",
    url = "https://apod.example/m16.jpg",
    hdUrl = null,
    mediaType = MediaType.IMAGE,
    copyright = null,
)

class GetTodayPictureUseCaseTest {

    @Test
    fun `returns the fetched picture when the network call succeeds`() = runTest {
        val useCase = GetTodayPictureUseCase(FakeTodayPictureRepository(Result.success(samplePicture)))

        val result = useCase()

        assertTrue(result.isSuccess)
        assertEquals(samplePicture, result.getOrNull())
    }

    @Test
    fun `falls back to the cache when the network call fails`() = runTest {
        val failure = Result.failure<AstronomyPicture>(RuntimeException("offline"))
        val useCase = GetTodayPictureUseCase(FakeTodayPictureRepository(failure, cached = samplePicture))

        val result = useCase()

        assertTrue(result.isSuccess)
        assertEquals(samplePicture, result.getOrNull())
    }

    @Test
    fun `surfaces failure when there is neither a fresh fetch nor a cache`() = runTest {
        val failure = Result.failure<AstronomyPicture>(RuntimeException("offline"))
        val useCase = GetTodayPictureUseCase(FakeTodayPictureRepository(failure, cached = null))

        val result = useCase()

        assertTrue(result.isFailure)
    }
}

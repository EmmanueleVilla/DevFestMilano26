package com.shadowings.devfestmilano26.apod.domain.usecase

import com.shadowings.devfestmilano26.apod.domain.model.AstronomyPicture
import com.shadowings.devfestmilano26.apod.domain.model.MediaType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

private val cachedPicture = AstronomyPicture(
    date = LocalDate.of(2026, 5, 14),
    title = "Title",
    explanation = "Explanation",
    url = "https://apod.example/pic.jpg",
    hdUrl = null,
    mediaType = MediaType.IMAGE,
    copyright = null,
)

private fun fakeRepository() =
    FakeTodayPictureRepository(fetchResult = Result.success(cachedPicture), cached = cachedPicture)

class ObserveCachedTodayPictureUseCaseTest {

    @Test
    fun `a null date resolves to the cached today entry`() = runTest {
        val useCase = ObserveCachedTodayPictureUseCase(fakeRepository())

        val result = useCase(null).first()

        assertEquals(cachedPicture, result)
    }

    @Test
    fun `a matching date resolves to the cached entry`() = runTest {
        val useCase = ObserveCachedTodayPictureUseCase(fakeRepository())

        val result = useCase(cachedPicture.date).first()

        assertEquals(cachedPicture, result)
    }

    @Test
    fun `a non-matching date resolves to null`() = runTest {
        val useCase = ObserveCachedTodayPictureUseCase(fakeRepository())

        val result = useCase(cachedPicture.date.minusDays(1)).first()

        assertNull(result)
    }
}

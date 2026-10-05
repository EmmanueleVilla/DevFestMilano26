package com.shadowings.devfestmilano26.apod.domain.usecase

import com.shadowings.devfestmilano26.apod.domain.model.AstronomyPicture
import com.shadowings.devfestmilano26.apod.domain.model.MediaType
import com.shadowings.devfestmilano26.apod.domain.model.SyncStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class ObserveFavoriteStatusUseCaseTest {

    private val picture = AstronomyPicture(
        date = LocalDate.of(2026, 5, 14),
        title = "Title",
        explanation = "Explanation",
        url = "https://apod.example/pic.jpg",
        hdUrl = null,
        mediaType = MediaType.IMAGE,
        copyright = null,
    )

    @Test
    fun `is null when the picture has not been saved`() = runTest {
        val repository = FakeFavoritesRepository()
        val useCase = ObserveFavoriteStatusUseCase(repository)

        assertNull(useCase(picture.date).first())
    }

    @Test
    fun `reflects the current status once saved`() = runTest {
        val repository = FakeFavoritesRepository()
        repository.saveFavorite(picture)
        val useCase = ObserveFavoriteStatusUseCase(repository)

        assertEquals(SyncStatus.LOCAL_ONLY, useCase(picture.date).first())
    }
}

package com.shadowings.devfestmilano26.apod.domain.usecase

import com.shadowings.devfestmilano26.apod.domain.model.AstronomyPicture
import com.shadowings.devfestmilano26.apod.domain.model.MediaType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class ToggleFavoriteUseCaseTest {

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
    fun `toggling an unsaved picture saves it`() = runTest {
        val repository = FakeFavoritesRepository()
        val useCase = ToggleFavoriteUseCase(repository)

        useCase(picture)

        assertNotNull(repository.observeFavoriteStatus(picture.date).first())
    }

    @Test
    fun `toggling a saved picture removes it`() = runTest {
        val repository = FakeFavoritesRepository()
        val useCase = ToggleFavoriteUseCase(repository)
        useCase(picture)

        useCase(picture)

        assertNull(repository.observeFavoriteStatus(picture.date).first())
    }
}

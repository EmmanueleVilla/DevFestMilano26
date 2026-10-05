package com.shadowings.devfestmilano26.apod.domain.usecase

import com.shadowings.devfestmilano26.apod.domain.model.AstronomyPicture
import com.shadowings.devfestmilano26.apod.domain.model.MediaType
import com.shadowings.devfestmilano26.apod.domain.model.SyncStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class SyncFavoritesOnLoginUseCaseTest {

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
    fun `every local-only favorite becomes synced with the signed-in uid`() = runTest {
        val repository = FakeFavoritesRepository()
        repository.saveFavorite(picture)
        val useCase = SyncFavoritesOnLoginUseCase(repository)

        useCase("uid-1")

        val favorite = repository.observeFavorites().first().single()
        assertEquals(SyncStatus.SYNCED, favorite.syncStatus)
        assertEquals("uid-1", favorite.ownerUid)
    }
}

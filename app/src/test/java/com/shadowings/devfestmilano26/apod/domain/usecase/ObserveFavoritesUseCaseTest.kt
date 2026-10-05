package com.shadowings.devfestmilano26.apod.domain.usecase

import com.shadowings.devfestmilano26.apod.domain.model.AstronomyPicture
import com.shadowings.devfestmilano26.apod.domain.model.MediaType
import com.shadowings.devfestmilano26.apod.domain.model.SyncStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class ObserveFavoritesUseCaseTest {

    private fun picture(day: Int) = AstronomyPicture(
        date = LocalDate.of(2026, 5, day),
        title = "Title $day",
        explanation = "Explanation",
        url = "https://apod.example/$day.jpg",
        hdUrl = null,
        mediaType = MediaType.IMAGE,
        copyright = null,
    )

    @Test
    fun `is empty when nothing has been saved`() = runTest {
        val useCase = ObserveFavoritesUseCase(FakeFavoritesRepository())

        assertTrue(useCase().first().isEmpty())
    }

    @Test
    fun `reflects every saved favorite`() = runTest {
        val repository = FakeFavoritesRepository()
        repository.saveFavorite(picture(1))
        repository.saveFavorite(picture(2))
        val useCase = ObserveFavoritesUseCase(repository)

        assertEquals(2, useCase().first().size)
    }

    @Test
    fun `local-only vs synced is preserved per favorite`() = runTest {
        val repository = FakeFavoritesRepository()
        repository.saveFavorite(picture(1))
        repository.migrateLocalFavoritesToAccount("uid-1")
        repository.saveFavorite(picture(2))
        val useCase = ObserveFavoritesUseCase(repository)

        val favorites = useCase().first().associateBy { it.picture.date }
        assertEquals(SyncStatus.SYNCED, favorites[picture(1).date]?.syncStatus)
        assertEquals(SyncStatus.LOCAL_ONLY, favorites[picture(2).date]?.syncStatus)
    }
}

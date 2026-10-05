package com.shadowings.devfestmilano26.apod.presentation.favorites

import com.shadowings.devfestmilano26.apod.domain.model.AstronomyPicture
import com.shadowings.devfestmilano26.apod.domain.model.MediaType
import com.shadowings.devfestmilano26.apod.domain.model.SyncStatus
import com.shadowings.devfestmilano26.apod.domain.usecase.FakeFavoritesRepository
import com.shadowings.devfestmilano26.apod.domain.usecase.ObserveFavoritesUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class FavoritesViewModelTest {

    @Before
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

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
    fun `empty repository maps to Empty state`() = runTest {
        val repository = FakeFavoritesRepository()
        val viewModel = FavoritesViewModel(ObserveFavoritesUseCase(repository), repository)
        testScheduler.advanceUntilIdle()

        assertTrue(viewModel.state.value is FavoritesState.Empty)
    }

    @Test
    fun `populated repository maps to Content state`() = runTest {
        val repository = FakeFavoritesRepository()
        repository.saveFavorite(picture(1))
        val viewModel = FavoritesViewModel(ObserveFavoritesUseCase(repository), repository)
        testScheduler.advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state is FavoritesState.Content)
        assertEquals(1, (state as FavoritesState.Content).favorites.size)
    }

    @Test
    fun `local-only and synced labels are both reflected`() = runTest {
        val repository = FakeFavoritesRepository()
        repository.saveFavorite(picture(1))
        repository.migrateLocalFavoritesToAccount("uid-1")
        repository.saveFavorite(picture(2))
        val viewModel = FavoritesViewModel(ObserveFavoritesUseCase(repository), repository)
        testScheduler.advanceUntilIdle()

        val favorites = (viewModel.state.value as FavoritesState.Content).favorites
            .associateBy { it.picture.date }
        assertEquals(SyncStatus.SYNCED, favorites[picture(1).date]?.syncStatus)
        assertEquals(SyncStatus.LOCAL_ONLY, favorites[picture(2).date]?.syncStatus)
    }
}

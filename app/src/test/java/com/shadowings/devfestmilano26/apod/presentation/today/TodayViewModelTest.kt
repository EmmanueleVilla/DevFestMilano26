package com.shadowings.devfestmilano26.apod.presentation.today

import com.shadowings.devfestmilano26.apod.domain.model.AstronomyPicture
import com.shadowings.devfestmilano26.apod.domain.model.MediaType
import com.shadowings.devfestmilano26.apod.domain.repository.TodayPictureRepository
import com.shadowings.devfestmilano26.apod.domain.usecase.FakeFavoritesRepository
import com.shadowings.devfestmilano26.apod.domain.usecase.GetTodayPictureUseCase
import com.shadowings.devfestmilano26.apod.domain.usecase.ObserveFavoriteStatusUseCase
import com.shadowings.devfestmilano26.apod.domain.usecase.ToggleFavoriteUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
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

private val samplePicture = AstronomyPicture(
    date = LocalDate.of(2026, 5, 14),
    title = "The Star-Forming Pillars of M16",
    explanation = "Dense cold molecular hydrogen pillars.",
    url = "https://apod.example/m16.jpg",
    hdUrl = null,
    mediaType = MediaType.IMAGE,
    copyright = null,
)

private class FakeTodayPictureRepository(
    private val result: Result<AstronomyPicture>,
) : TodayPictureRepository {
    override suspend fun getTodayPicture(): Result<AstronomyPicture> = result
    override fun observeCachedTodayPicture(): Flow<AstronomyPicture?> = flowOf(null)
}

@OptIn(ExperimentalCoroutinesApi::class)
class TodayViewModelTest {

    @Before
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun buildViewModel(
        fetchResult: Result<AstronomyPicture> = Result.success(samplePicture),
        favoritesRepository: FakeFavoritesRepository = FakeFavoritesRepository(),
    ): TodayViewModel {
        val todayRepository = FakeTodayPictureRepository(fetchResult)
        return TodayViewModel(
            getTodayPicture = GetTodayPictureUseCase(todayRepository),
            observeFavoriteStatus = ObserveFavoriteStatusUseCase(favoritesRepository),
            toggleFavorite = ToggleFavoriteUseCase(favoritesRepository),
        )
    }

    @Test
    fun `loads content on start`() = runTest {
        val viewModel = buildViewModel()
        testScheduler.advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state is TodayState.Content)
        assertEquals(samplePicture, (state as TodayState.Content).picture)
    }

    @Test
    fun `expand toggles the explanation flag`() = runTest {
        val viewModel = buildViewModel()
        testScheduler.advanceUntilIdle()

        viewModel.onIntent(TodayIntent.Expand)
        testScheduler.advanceUntilIdle()

        val state = viewModel.state.value as TodayState.Content
        assertTrue(state.isExplanationExpanded)
    }

    @Test
    fun `toggling favorite updates favoriteStatus`() = runTest {
        val viewModel = buildViewModel()
        testScheduler.advanceUntilIdle()

        viewModel.onIntent(TodayIntent.ToggleFavorite)
        testScheduler.advanceUntilIdle()

        val state = viewModel.state.value as TodayState.Content
        assertEquals(
            com.shadowings.devfestmilano26.apod.domain.model.SyncStatus.LOCAL_ONLY,
            state.favoriteStatus,
        )
    }

    @Test
    fun `a fetch failure with no cache surfaces an error state`() = runTest {
        val viewModel = buildViewModel(fetchResult = Result.failure(RuntimeException("offline")))
        testScheduler.advanceUntilIdle()

        assertTrue(viewModel.state.value is TodayState.Error)
    }

    @Test
    fun `retry re-attempts loading from the error state`() = runTest {
        val viewModel = buildViewModel(fetchResult = Result.failure(RuntimeException("offline")))
        testScheduler.advanceUntilIdle()
        assertTrue(viewModel.state.value is TodayState.Error)

        viewModel.onIntent(TodayIntent.Retry)
        testScheduler.advanceUntilIdle()

        assertTrue(viewModel.state.value is TodayState.Error)
    }
}

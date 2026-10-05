package com.shadowings.devfestmilano26.apod.presentation.detail

import androidx.lifecycle.SavedStateHandle
import com.shadowings.devfestmilano26.apod.domain.model.AstronomyPicture
import com.shadowings.devfestmilano26.apod.domain.model.MediaType
import com.shadowings.devfestmilano26.apod.domain.usecase.FakeFavoritesRepository
import com.shadowings.devfestmilano26.apod.domain.usecase.FakeTodayPictureRepository
import com.shadowings.devfestmilano26.apod.domain.usecase.ObserveCachedTodayPictureUseCase
import com.shadowings.devfestmilano26.apod.domain.usecase.ObserveFavoriteStatusUseCase
import com.shadowings.devfestmilano26.apod.domain.usecase.ToggleFavoriteUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

private val picture = AstronomyPicture(
    date = LocalDate.of(2026, 5, 14),
    title = "The Star-Forming Pillars of M16",
    explanation = "Dense cold molecular hydrogen pillars.",
    url = "https://apod.example/m16.jpg",
    hdUrl = null,
    mediaType = MediaType.IMAGE,
    copyright = "Jane Astronomer",
)

@OptIn(ExperimentalCoroutinesApi::class)
class DetailViewModelTest {

    @Before
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun buildViewModel(
        date: String? = picture.date.toString(),
        favoritesRepository: FakeFavoritesRepository = FakeFavoritesRepository(),
    ): DetailViewModel {
        val todayRepository = FakeTodayPictureRepository(Result.success(picture), cached = picture)
        val savedStateHandle = SavedStateHandle(mapOf("date" to date))
        return DetailViewModel(
            savedStateHandle = savedStateHandle,
            observeCachedTodayPicture = ObserveCachedTodayPictureUseCase(todayRepository),
            observeFavoriteStatus = ObserveFavoriteStatusUseCase(favoritesRepository),
            toggleFavorite = ToggleFavoriteUseCase(favoritesRepository),
        )
    }

    @Test
    fun `loads content for the requested date`() = runTest {
        val viewModel = buildViewModel()
        testScheduler.advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state is DetailState.Content)
        assertEquals(picture, (state as DetailState.Content).picture)
    }

    @Test
    fun `credit is only present when the picture has copyright`() = runTest {
        val viewModel = buildViewModel()
        testScheduler.advanceUntilIdle()

        val state = viewModel.state.value as DetailState.Content
        assertEquals("Jane Astronomer", state.picture.copyright)
    }

    @Test
    fun `toggling save marks the entry favorited`() = runTest {
        val viewModel = buildViewModel()
        testScheduler.advanceUntilIdle()
        assertNull((viewModel.state.value as DetailState.Content).favoriteStatus)

        viewModel.onIntent(DetailIntent.ToggleSave)
        testScheduler.advanceUntilIdle()

        assertTrue((viewModel.state.value as DetailState.Content).favoriteStatus != null)
    }

    @Test
    fun `share emits an effect with the title and permalink`() = runTest {
        val viewModel = buildViewModel()
        testScheduler.advanceUntilIdle()

        var emitted: DetailEffect? = null
        val job = launch { emitted = viewModel.effects.first() }
        viewModel.onIntent(DetailIntent.Share)
        testScheduler.advanceUntilIdle()
        job.cancel()

        val effect = emitted
        assertTrue(effect is DetailEffect.OpenShareSheet)
        assertEquals("The Star-Forming Pillars of M16", (effect as DetailEffect.OpenShareSheet).title)
    }
}

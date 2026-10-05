package com.shadowings.devfestmilano26.apod.presentation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shadowings.devfestmilano26.apod.domain.usecase.ObserveCachedTodayPictureUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import com.shadowings.devfestmilano26.apod.domain.usecase.ObserveFavoriteStatusUseCase
import com.shadowings.devfestmilano26.apod.domain.usecase.ToggleFavoriteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

/** Builds the NASA APOD permalink for [date], e.g. `apod.nasa.gov/apod/ap260514.html`. */
fun permalinkFor(date: LocalDate): String {
    val yy = (date.year % 100).toString().padStart(2, '0')
    val mm = date.monthValue.toString().padStart(2, '0')
    val dd = date.dayOfMonth.toString().padStart(2, '0')
    return "https://apod.nasa.gov/apod/ap$yy$mm$dd.html"
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class DetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeCachedTodayPicture: ObserveCachedTodayPictureUseCase,
    private val observeFavoriteStatus: ObserveFavoriteStatusUseCase,
    private val toggleFavorite: ToggleFavoriteUseCase,
) : ViewModel() {

    private val requestedDate: LocalDate? =
        savedStateHandle.get<String>("date")?.let(LocalDate::parse)

    private val _state = MutableStateFlow<DetailState>(DetailState.Loading)
    val state: StateFlow<DetailState> = _state.asStateFlow()

    private val effectChannel = Channel<DetailEffect>()
    val effects: Flow<DetailEffect> = effectChannel.receiveAsFlow()

    init {
        observeCachedTodayPicture(requestedDate)
            .flatMapLatest { picture ->
                if (picture == null) {
                    kotlinx.coroutines.flow.flowOf(
                        DetailState.Error("Could not load this log entry") as DetailState,
                    )
                } else {
                    observeFavoriteStatus(picture.date).combine(
                        kotlinx.coroutines.flow.flowOf(picture),
                    ) { status, pic -> DetailState.Content(pic, status) as DetailState }
                }
            }
            .onEach { _state.value = it }
            .launchIn(viewModelScope)
    }

    fun onIntent(intent: DetailIntent) {
        when (intent) {
            DetailIntent.ToggleSave -> onToggleSave()
            DetailIntent.Share -> onShare()
            DetailIntent.OpenPermalink -> onOpenPermalink()
        }
    }

    private fun onToggleSave() {
        val current = _state.value
        if (current is DetailState.Content) {
            viewModelScope.launch { toggleFavorite(current.picture) }
        }
    }

    private fun onShare() {
        val current = _state.value
        if (current is DetailState.Content) {
            viewModelScope.launch {
                effectChannel.send(
                    DetailEffect.OpenShareSheet(current.picture.title, permalinkFor(current.picture.date)),
                )
            }
        }
    }

    private fun onOpenPermalink() {
        val current = _state.value
        if (current is DetailState.Content) {
            viewModelScope.launch {
                effectChannel.send(DetailEffect.OpenBrowser(permalinkFor(current.picture.date)))
            }
        }
    }
}

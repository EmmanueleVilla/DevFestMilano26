package com.shadowings.devfestmilano26.apod.presentation.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shadowings.devfestmilano26.apod.domain.usecase.GetTodayPictureUseCase
import com.shadowings.devfestmilano26.apod.domain.usecase.ObserveFavoriteStatusUseCase
import com.shadowings.devfestmilano26.apod.domain.usecase.ToggleFavoriteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TodayViewModel @Inject constructor(
    private val getTodayPicture: GetTodayPictureUseCase,
    private val observeFavoriteStatus: ObserveFavoriteStatusUseCase,
    private val toggleFavorite: ToggleFavoriteUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow<TodayState>(TodayState.Loading)
    val state: StateFlow<TodayState> = _state.asStateFlow()

    private val effectChannel = Channel<TodayEffect>()
    val effects: Flow<TodayEffect> = effectChannel.receiveAsFlow()

    init {
        load()
    }

    fun onIntent(intent: TodayIntent) {
        when (intent) {
            TodayIntent.Expand -> toggleExpanded()
            TodayIntent.ToggleFavorite -> onToggleFavorite()
            TodayIntent.Retry -> load()
            TodayIntent.OpenDetail -> onOpenDetail()
        }
    }

    private fun load() {
        _state.value = TodayState.Loading
        viewModelScope.launch {
            val result = getTodayPicture()
            result.fold(
                onSuccess = { picture ->
                    _state.value = TodayState.Content(
                        picture = picture,
                        favoriteStatus = null,
                        isExplanationExpanded = false,
                    )
                    observeFavoriteStatus(picture.date)
                        .onEach { status ->
                            val current = _state.value
                            if (current is TodayState.Content) {
                                _state.value = current.copy(favoriteStatus = status)
                            }
                        }
                        .launchIn(viewModelScope)
                },
                onFailure = { error ->
                    _state.value = TodayState.Error(error.message ?: "Unable to load today's entry")
                },
            )
        }
    }

    private fun toggleExpanded() {
        val current = _state.value
        if (current is TodayState.Content) {
            _state.value = current.copy(isExplanationExpanded = !current.isExplanationExpanded)
        }
    }

    private fun onToggleFavorite() {
        val current = _state.value
        if (current is TodayState.Content) {
            viewModelScope.launch { toggleFavorite(current.picture) }
        }
    }

    private fun onOpenDetail() {
        val current = _state.value
        if (current is TodayState.Content) {
            viewModelScope.launch {
                effectChannel.send(TodayEffect.NavigateToDetail(current.picture.date.toString()))
            }
        }
    }
}

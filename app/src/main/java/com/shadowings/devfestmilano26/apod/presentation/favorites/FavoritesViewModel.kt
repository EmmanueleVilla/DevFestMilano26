package com.shadowings.devfestmilano26.apod.presentation.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shadowings.devfestmilano26.apod.domain.repository.FavoritesRepository
import com.shadowings.devfestmilano26.apod.domain.usecase.ObserveFavoritesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    observeFavorites: ObserveFavoritesUseCase,
    private val favoritesRepository: FavoritesRepository,
) : ViewModel() {

    private val _state = MutableStateFlow<FavoritesState>(FavoritesState.Loading)
    val state: StateFlow<FavoritesState> = _state.asStateFlow()

    init {
        observeFavorites()
            .map { favorites ->
                if (favorites.isEmpty()) FavoritesState.Empty else FavoritesState.Content(favorites)
            }
            .onEach { _state.value = it }
            .launchIn(viewModelScope)
    }

    fun onIntent(intent: FavoritesIntent) {
        when (intent) {
            is FavoritesIntent.OpenFavorite -> Unit // navigation handled by the screen
            FavoritesIntent.Retry -> Unit // observeFavorites is a live Flow; nothing to retry manually
            FavoritesIntent.SyncNow -> onSyncNow()
        }
    }

    private fun onSyncNow() {
        viewModelScope.launch { favoritesRepository.syncNow() }
    }
}

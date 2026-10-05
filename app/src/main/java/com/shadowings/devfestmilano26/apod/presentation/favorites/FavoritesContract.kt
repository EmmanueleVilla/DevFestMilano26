package com.shadowings.devfestmilano26.apod.presentation.favorites

import com.shadowings.devfestmilano26.apod.domain.model.Favorite

sealed interface FavoritesState {
    data object Loading : FavoritesState
    data object Empty : FavoritesState
    data class Content(val favorites: List<Favorite>) : FavoritesState
    data class Error(val message: String) : FavoritesState
}

sealed interface FavoritesIntent {
    data class OpenFavorite(val date: String) : FavoritesIntent
    data object Retry : FavoritesIntent
    data object SyncNow : FavoritesIntent
}

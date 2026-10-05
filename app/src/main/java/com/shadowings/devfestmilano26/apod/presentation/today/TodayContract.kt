package com.shadowings.devfestmilano26.apod.presentation.today

import com.shadowings.devfestmilano26.apod.domain.model.AstronomyPicture
import com.shadowings.devfestmilano26.apod.domain.model.SyncStatus

sealed interface TodayState {
    data object Loading : TodayState
    data class Content(
        val picture: AstronomyPicture,
        val favoriteStatus: SyncStatus?,
        val isExplanationExpanded: Boolean,
    ) : TodayState
    data class Error(val message: String) : TodayState
}

sealed interface TodayIntent {
    data object Expand : TodayIntent
    data object ToggleFavorite : TodayIntent
    data object Retry : TodayIntent
    data object OpenDetail : TodayIntent
}

sealed interface TodayEffect {
    data class NavigateToDetail(val date: String) : TodayEffect
}

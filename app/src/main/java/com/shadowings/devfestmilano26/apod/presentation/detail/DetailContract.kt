package com.shadowings.devfestmilano26.apod.presentation.detail

import com.shadowings.devfestmilano26.apod.domain.model.AstronomyPicture
import com.shadowings.devfestmilano26.apod.domain.model.SyncStatus

sealed interface DetailState {
    data object Loading : DetailState
    data class Content(
        val picture: AstronomyPicture,
        val favoriteStatus: SyncStatus?,
    ) : DetailState
    data class Error(val message: String) : DetailState
}

sealed interface DetailIntent {
    data object ToggleSave : DetailIntent
    data object Share : DetailIntent
    data object OpenPermalink : DetailIntent
}

sealed interface DetailEffect {
    data class OpenShareSheet(val title: String, val permalink: String) : DetailEffect
    data class OpenBrowser(val url: String) : DetailEffect
}

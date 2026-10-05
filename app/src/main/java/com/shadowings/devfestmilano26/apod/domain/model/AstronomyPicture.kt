package com.shadowings.devfestmilano26.apod.domain.model

import java.time.LocalDate

enum class MediaType { IMAGE, VIDEO }

/**
 * A single day's NASA APOD entry. Identity is [date] -- NASA publishes at most one entry per
 * calendar date. [hdUrl] is only ever non-null when [mediaType] == [MediaType.IMAGE].
 */
data class AstronomyPicture(
    val date: LocalDate,
    val title: String,
    val explanation: String,
    val url: String,
    val hdUrl: String?,
    val mediaType: MediaType,
    val copyright: String?,
)

package com.shadowings.devfestmilano26.apod.data.remote

import com.shadowings.devfestmilano26.apod.domain.model.AstronomyPicture
import com.shadowings.devfestmilano26.apod.domain.model.MediaType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
data class ApodDto(
    val date: String,
    val title: String,
    val explanation: String,
    val url: String,
    val hdurl: String? = null,
    @SerialName("media_type") val mediaType: String,
    val copyright: String? = null,
)

fun ApodDto.toDomain(): AstronomyPicture {
    val type = if (mediaType == "video") MediaType.VIDEO else MediaType.IMAGE
    return AstronomyPicture(
        date = LocalDate.parse(date),
        title = title,
        explanation = explanation,
        url = url,
        hdUrl = if (type == MediaType.IMAGE) hdurl else null,
        mediaType = type,
        copyright = copyright,
    )
}

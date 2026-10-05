package com.shadowings.devfestmilano26.apod.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Single-row cache of the last successfully fetched "today" [com.shadowings.devfestmilano26.apod.domain.model.AstronomyPicture].
 * Always overwritten -- only the most recent successful fetch is kept.
 */
@Entity(tableName = "cached_today")
data class CachedTodayEntity(
    @PrimaryKey val id: Int = SINGLETON_ID,
    val date: String,
    val title: String,
    val explanation: String,
    val url: String,
    val hdUrl: String?,
    val mediaType: String,
    val copyright: String?,
    val fetchedAtEpochMillis: Long,
) {
    companion object {
        const val SINGLETON_ID = 0
    }
}

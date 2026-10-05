package com.shadowings.devfestmilano26.apod.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room mirror of [com.shadowings.devfestmilano26.apod.domain.model.Favorite]. Primary key [date]
 * means saving an already-favorited date again is an update, not a duplicate insert (toggle
 * semantics live in the repository layer via insert-or-replace + delete-by-date).
 */
@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val date: String,
    val title: String,
    val explanation: String,
    val url: String,
    val hdUrl: String?,
    val mediaType: String,
    val copyright: String?,
    val syncStatus: String,
    val ownerUid: String?,
    val savedAtEpochMillis: Long,
)

package com.shadowings.devfestmilano26.apod.domain.model

import java.time.LocalDate

enum class SyncStatus { LOCAL_ONLY, SYNCED }

/**
 * A user's saved reference to an [AstronomyPicture]. A row with [syncStatus] == SYNCED always
 * has a non-null [ownerUid]; a row with [syncStatus] == LOCAL_ONLY always has a null [ownerUid].
 */
data class Favorite(
    val picture: AstronomyPicture,
    val syncStatus: SyncStatus,
    val ownerUid: String?,
    val savedAtEpochMillis: Long,
) {
    init {
        require((syncStatus == SyncStatus.SYNCED) == (ownerUid != null)) {
            "ownerUid must be non-null iff syncStatus is SYNCED"
        }
    }
}

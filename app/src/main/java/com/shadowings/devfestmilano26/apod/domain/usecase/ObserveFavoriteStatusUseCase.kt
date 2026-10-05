package com.shadowings.devfestmilano26.apod.domain.usecase

import com.shadowings.devfestmilano26.apod.domain.model.SyncStatus
import com.shadowings.devfestmilano26.apod.domain.repository.FavoritesRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject

class ObserveFavoriteStatusUseCase @Inject constructor(
    private val repository: FavoritesRepository,
) {
    operator fun invoke(date: LocalDate): Flow<SyncStatus?> = repository.observeFavoriteStatus(date)
}

package com.shadowings.devfestmilano26.apod.domain.usecase

import com.shadowings.devfestmilano26.apod.domain.model.AstronomyPicture
import com.shadowings.devfestmilano26.apod.domain.repository.TodayPictureRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject

/**
 * Resolves the full [AstronomyPicture] for [date] from the Today cache. Since archive browsing
 * is out of scope (spec.md Assumptions), the only entry ever cached is "today's", so a null or
 * mismatched [date] both mean "show today's entry" -- the Log/Detail tab's default when nothing
 * specific was selected.
 */
class ObserveCachedTodayPictureUseCase @Inject constructor(
    private val repository: TodayPictureRepository,
) {
    operator fun invoke(date: LocalDate?): Flow<AstronomyPicture?> =
        repository.observeCachedTodayPicture().map { cached ->
            if (date == null || cached?.date == date) cached else null
        }
}

package com.shadowings.devfestmilano26.apod.presentation.favorites

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.shadowings.devfestmilano26.apod.domain.model.AstronomyPicture
import com.shadowings.devfestmilano26.apod.domain.model.Favorite
import com.shadowings.devfestmilano26.apod.domain.model.MediaType
import com.shadowings.devfestmilano26.apod.domain.model.SyncStatus
import com.shadowings.devfestmilano26.ui.theme.DevFestMilano26Theme
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

class FavoritesScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun favorite(day: Int, status: SyncStatus) = Favorite(
        picture = AstronomyPicture(
            date = LocalDate.of(2026, 5, day),
            title = "Title $day",
            explanation = "Explanation",
            url = "https://apod.example/$day.jpg",
            hdUrl = null,
            mediaType = MediaType.IMAGE,
            copyright = null,
        ),
        syncStatus = status,
        ownerUid = if (status == SyncStatus.SYNCED) "uid-1" else null,
        savedAtEpochMillis = 0L,
    )

    @Test
    fun emptyStateShowsGuidance() {
        composeTestRule.setContent {
            DevFestMilano26Theme { FavoritesEmpty() }
        }

        composeTestRule.onNodeWithText("NO ENTRIES YET").assertIsDisplayed()
    }

    @Test
    fun contentStateShowsEachFavoriteWithItsSyncLabel() {
        composeTestRule.setContent {
            DevFestMilano26Theme {
                FavoritesGrid(
                    favorites = listOf(
                        favorite(1, SyncStatus.SYNCED),
                        favorite(2, SyncStatus.LOCAL_ONLY),
                    ),
                    onOpenFavorite = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Title 1").assertIsDisplayed()
        composeTestRule.onNodeWithText("SYNCED").assertIsDisplayed()
        composeTestRule.onNodeWithText("Title 2").assertIsDisplayed()
        composeTestRule.onNodeWithText("LOCAL").assertIsDisplayed()
    }
}

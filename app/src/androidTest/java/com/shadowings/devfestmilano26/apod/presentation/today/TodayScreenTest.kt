package com.shadowings.devfestmilano26.apod.presentation.today

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.shadowings.devfestmilano26.apod.domain.model.AstronomyPicture
import com.shadowings.devfestmilano26.apod.domain.model.MediaType
import com.shadowings.devfestmilano26.ui.theme.DevFestMilano26Theme
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

class TodayScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val picture = AstronomyPicture(
        date = LocalDate.of(2026, 5, 14),
        title = "The Star-Forming Pillars of M16",
        explanation = "Dense cold molecular hydrogen pillars.",
        url = "https://apod.example/m16.jpg",
        hdUrl = null,
        mediaType = MediaType.IMAGE,
        copyright = null,
    )

    @Test
    fun contentStateShowsTitleAndDate() {
        composeTestRule.setContent {
            DevFestMilano26Theme {
                TodayContent(
                    state = TodayState.Content(picture, favoriteStatus = null, isExplanationExpanded = false),
                    onToggleFavorite = {},
                    onExpand = {},
                    onOpenDetail = {},
                )
            }
        }

        composeTestRule.onNodeWithText(picture.title).assertIsDisplayed()
        composeTestRule.onNodeWithText(picture.date.toString()).assertIsDisplayed()
    }

    @Test
    fun loadingStateShowsReadingSkyLabel() {
        composeTestRule.setContent {
            DevFestMilano26Theme { TodayLoading() }
        }

        composeTestRule.onNodeWithText("READING SKY…").assertIsDisplayed()
    }

    @Test
    fun errorStateShowsSignalLostAndRetry() {
        composeTestRule.setContent {
            DevFestMilano26Theme { TodayError("Network unavailable", onRetry = {}) }
        }

        composeTestRule.onNodeWithText("SIGNAL LOST").assertIsDisplayed()
        composeTestRule.onNodeWithText("RETRY").assertIsDisplayed()
    }
}

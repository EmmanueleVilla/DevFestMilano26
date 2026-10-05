package com.shadowings.devfestmilano26.apod.presentation.detail

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.shadowings.devfestmilano26.apod.domain.model.AstronomyPicture
import com.shadowings.devfestmilano26.apod.domain.model.MediaType
import com.shadowings.devfestmilano26.ui.theme.DevFestMilano26Theme
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

class DetailScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val picture = AstronomyPicture(
        date = LocalDate.of(2026, 5, 14),
        title = "The Star-Forming Pillars of M16",
        explanation = "Dense cold molecular hydrogen pillars.",
        url = "https://apod.example/m16.jpg",
        hdUrl = null,
        mediaType = MediaType.IMAGE,
        copyright = "Jane Astronomer",
    )

    @Test
    fun contentStateShowsExplanationAndCredit() {
        composeTestRule.setContent {
            DevFestMilano26Theme {
                DetailContent(
                    state = DetailState.Content(picture, favoriteStatus = null),
                    onToggleSave = {},
                    onShare = {},
                    onOpenPermalink = {},
                )
            }
        }

        composeTestRule.onNodeWithText(picture.explanation).assertIsDisplayed()
        composeTestRule.onNodeWithText("CREDIT: Jane Astronomer").assertIsDisplayed()
    }

    @Test
    fun creditIsOmittedWhenAbsent() {
        composeTestRule.setContent {
            DevFestMilano26Theme {
                DetailContent(
                    state = DetailState.Content(picture.copy(copyright = null), favoriteStatus = null),
                    onToggleSave = {},
                    onShare = {},
                    onOpenPermalink = {},
                )
            }
        }

        composeTestRule.onNodeWithText(picture.explanation).assertIsDisplayed()
    }

    @Test
    fun errorStateShowsSignalLost() {
        composeTestRule.setContent {
            DevFestMilano26Theme { DetailError("Could not load this log entry") }
        }

        composeTestRule.onNodeWithText("SIGNAL LOST").assertIsDisplayed()
    }
}

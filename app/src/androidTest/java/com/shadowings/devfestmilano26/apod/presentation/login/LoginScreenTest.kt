package com.shadowings.devfestmilano26.apod.presentation.login

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.shadowings.devfestmilano26.ui.theme.DevFestMilano26Theme
import org.junit.Rule
import org.junit.Test

class LoginScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun defaultStateShowsAllThreeSignInPaths() {
        composeTestRule.setContent {
            DevFestMilano26Theme { LoginContent(state = LoginState.Default, onIntent = {}) }
        }

        composeTestRule.onNodeWithText("AUTHORIZE & SYNC LOGS").assertIsDisplayed()
        composeTestRule.onNodeWithText("SIGN IN VIA GOOGLE IDENTITY").assertIsDisplayed()
        composeTestRule.onNodeWithText("Continue without an account").assertIsDisplayed()
    }

    @Test
    fun errorStateShowsTheErrorMessage() {
        composeTestRule.setContent {
            DevFestMilano26Theme { LoginContent(state = LoginState.Error("Invalid credentials"), onIntent = {}) }
        }

        composeTestRule.onNodeWithText("Invalid credentials").assertIsDisplayed()
    }
}

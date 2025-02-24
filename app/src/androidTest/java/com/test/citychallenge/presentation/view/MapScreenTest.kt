package com.test.citychallenge.presentation.view

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.test.citychallenge.presentation.model.CityUIItem
import com.test.citychallenge.presentation.view.screen.MapScreen
import com.test.citychallenge.presentation.viewmodel.CityEvent
import com.test.citychallenge.presentation.viewmodel.CityViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class MapScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val fakeCity = CityUIItem(
        id = 1L,
        name = "Test City",
        country = "Test Country",
        lat = 1.0,
        lon = 2.0,
        isFavorite = true
    )

    private val fakeSelectedCityFlow = MutableStateFlow<CityUIItem?>(fakeCity)
    private val fakeViewModel = mockk<CityViewModel>(relaxed = true) {
        every { selectedCity } returns fakeSelectedCityFlow
    }

    @Test
    fun mapScreen_displaysTopAppBar_andHandlesBackNavigation() {
        var backClicked = false

        // Set the content. (Assuming portrait orientation so that TopAppBar is visible.)
        composeTestRule.setContent {
            MaterialTheme {
                Surface {
                    MapScreen(
                        navigationCity = fakeCity,
                        onBack = { backClicked = true },
                        viewModel = fakeViewModel
                    )
                }
            }
        }

        // Verify that the TopAppBar title displays the city name.
        composeTestRule.onNodeWithText("Test City").assertIsDisplayed()

        // Find the back button (with content description "Back") and click it.
        composeTestRule.onNodeWithContentDescription("Back").performClick()
        assertTrue("Back callback should have been invoked", backClicked)
    }

    @Test
    fun mapScreen_toggleInfoIcon_showsCityInformation() {
        // Set the content with our fake view model.
        composeTestRule.setContent {
            MaterialTheme {
                Surface {
                    MapScreen(
                        navigationCity = fakeCity,
                        onBack = {},
                        viewModel = fakeViewModel
                    )
                }
            }
        }

        // Initially, the detailed info text is not displayed.
        composeTestRule.onNodeWithText("Test Country").assertDoesNotExist()

        // Click the info icon (with content description "City Info").
        composeTestRule.onNodeWithContentDescription("City Info").performClick()

        // Verify that the city details are now displayed.
        composeTestRule.onNodeWithText("Test Country").assertIsDisplayed()
        composeTestRule.onNodeWithText("Lat: 1.0, Lon: 2.0").assertIsDisplayed()
        composeTestRule.onNodeWithText("Favorite").assertIsDisplayed()
    }

    @Test
    fun mapScreen_callsOnLoadInitialCityEvent() {
        // We use a fresh fake view model to verify the call.
        val viewModelForTest = mockk<CityViewModel>(relaxed = true) {
            every { selectedCity } returns MutableStateFlow(fakeCity)
        }

        composeTestRule.setContent {
            MaterialTheme {
                Surface {
                    MapScreen(
                        navigationCity = fakeCity,
                        onBack = {},
                        viewModel = viewModelForTest
                    )
                }
            }
        }

        // Wait for composition and LaunchedEffects to settle.
        composeTestRule.waitForIdle()

        // Verify that the OnLoadInitialCity event was dispatched.
        verify { viewModelForTest.onEvent(CityEvent.OnLoadInitialCity) }
    }

    @Test
    fun mapScreen_displaysAttributionText() {
        // Verify that the attribution text for OpenStreetMap is visible.
        composeTestRule.setContent {
            MaterialTheme {
                Surface {
                    MapScreen(
                        navigationCity = fakeCity,
                        onBack = {},
                        viewModel = fakeViewModel
                    )
                }
            }
        }

        composeTestRule.onNodeWithText("© OpenStreetMap contributors").assertIsDisplayed()
    }
}

package com.test.citychallenge.presentation.view

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.paging.PagingData
import com.test.citychallenge.presentation.model.CityUIItem
import com.test.citychallenge.presentation.view.screen.HomeScreen
import com.test.citychallenge.presentation.viewmodel.CityEvent
import com.test.citychallenge.presentation.viewmodel.CityViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test

class HomeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val fakeCity = CityUIItem(
        id = 1L,
        name = "Test City",
        country = "Test Country",
        lat = 12.34,
        lon = 56.78,
        isFavorite = false
    )

    private val fakePagingData = PagingData.from(listOf(fakeCity))

    // We relax most functions and supply controlled flows for cities and isInitialLoading.
    private val fakeViewModel = mockk<CityViewModel>(relaxed = true) {
        every { cities } returns flowOf(fakePagingData)
        every { isInitialLoading } returns MutableStateFlow(false)
    }

    @Test
    fun homeScreen_displaysCityList() {
        composeTestRule.setContent {
            HomeScreen(viewModel = fakeViewModel)
        }

        // Verify that the search text field with label "Search cities" is displayed.
        composeTestRule.onNodeWithText("Search cities").assertIsDisplayed()

        // Verify that the city list item displays the city name, country, and coordinates.
        composeTestRule.onNodeWithText("Test City, Test Country").assertIsDisplayed()
        composeTestRule.onNodeWithText("Lat: 12.34, Lon: 56.78").assertIsDisplayed()
    }

    @Test
    fun homeScreen_searchInput_triggersOnSearchQueryChanged() {
        composeTestRule.setContent {
            HomeScreen(viewModel = fakeViewModel)
        }
        // Enter text into the search field.
        val searchQuery = "New York"
        composeTestRule.onNode(hasText("Search cities")).performTextInput(searchQuery)

        // Verify that the onEvent function was called with a CityEvent.OnSearchQueryChanged event carrying the entered text.
        verify { fakeViewModel.onEvent(match { it is CityEvent.OnSearchQueryChanged && it.query == searchQuery }) }
    }

    @Test
    fun homeScreen_displaysLoadingIndicator_whenInitialLoadingIsTrue() {
        // For this test, we create a new fake view model with isInitialLoading set to true.
        val loadingState = MutableStateFlow(true)
        val loadingViewModel = mockk<CityViewModel>(relaxed = true) {
            every { cities } returns flowOf(PagingData.empty())
            every { isInitialLoading } returns loadingState
        }

        composeTestRule.setContent {
            HomeScreen(viewModel = loadingViewModel)
        }

        composeTestRule.onAllNodesWithTag("LoadingIndicator").assertCountEquals(2)
    }

    @Test
    fun homeScreen_favoritesCheckbox_triggersOnFavoriteFilterChanged() {
        composeTestRule.setContent {
            HomeScreen(viewModel = fakeViewModel)
        }

        composeTestRule.onNodeWithTag("favoritesCheckbox").performClick()

        // Verify that onEvent is called with a CityEvent.OnFavoriteFilterChanged event where enabled == true.
        verify { fakeViewModel.onEvent(match { it is CityEvent.OnFavoriteFilterChanged && it.enabled }) }
    }
}

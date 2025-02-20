package com.test.citychallenge.presentation.viewmodel

import com.test.citychallenge.common.Response
import com.test.citychallenge.domain.model.CityModel
import com.test.citychallenge.domain.model.Coord
import com.test.citychallenge.domain.usecase.CityFilterUseCase
import com.test.citychallenge.domain.usecase.SyncCitiesUseCase
import com.test.citychallenge.domain.usecase.ToggleFavoriteUseCase
import com.test.citychallenge.presentation.mapper.toUIItem
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CityViewModelTest {

    // Set Main dispatcher to a TestDispatcher
    private val testDispatcher: TestDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private lateinit var syncCitiesUseCase: SyncCitiesUseCase
    private lateinit var cityFilterUseCase: CityFilterUseCase
    private lateinit var toggleFavoriteUseCase: ToggleFavoriteUseCase
    private lateinit var viewModel: CityViewModel

    // A fake CityModel for testing.
    private fun fakeCityModel(): CityModel {
        return CityModel(
            id = 1,
            name = "TestCity",
            country = "TC",
            coord = Coord(lat = 10.0, lon = 20.0),
            isFavorite = false
        )
    }

    // Provide a fake flow for filtering use case.
    private fun fakeCityFilterFlow(): Flow<Response<List<CityModel>>> {
        return flowOf(Response.Success(listOf(fakeCityModel())))
    }

    @Before
    fun setUp() {
        // Set Main to our test dispatcher.
        kotlinx.coroutines.Dispatchers.setMain(testDispatcher)

        // Create mocks for use cases.
        syncCitiesUseCase = mockk()
        cityFilterUseCase = mockk()
        toggleFavoriteUseCase = mockk()

        // Default behavior: sync returns success.
        coEvery { syncCitiesUseCase() } returns Response.Success(true)
        // Default behavior: city filter returns a flow with one fake city.
        every { cityFilterUseCase(any(), any()) } returns fakeCityFilterFlow()
        // Default behavior: toggleFavorite simply runs.
        coEvery { toggleFavoriteUseCase(any()) } returns Response.Success(Unit)

        // Create the ViewModel instance.
        viewModel = CityViewModel(syncCitiesUseCase, cityFilterUseCase, toggleFavoriteUseCase)
    }

    @After
    fun tearDown() {
        kotlinx.coroutines.Dispatchers.resetMain()
        testScope.cancel()
    }

    @Test
    fun `when syncCities returns success, uiState eventually becomes Success`() =
        runTest(testDispatcher) {
            // Allow the initial sync and search observation to run.
            advanceTimeBy(400) // account for debounce delay
            val currentState = viewModel.uiState.value
            assertTrue(currentState is UIState.Success)
            currentState as UIState.Success
            assertEquals(1, currentState.cities.size)
            assertEquals("TestCity", currentState.cities.first().name)
        }

    @Test
    fun `onEvent OnSearchQueryChanged triggers cityFilterUseCase with updated query`() =
        runTest(testDispatcher) {
            viewModel.onEvent(CityEvent.OnSearchQueryChanged("NewQuery"))
            advanceTimeBy(400) // wait for debounce
            // Verify that cityFilterUseCase is called with "NewQuery" and favorites flag false (default)
            verify { cityFilterUseCase("NewQuery", false) }
        }

    @Test
    fun `onEvent OnFavoriteFilterChanged updates onlyFavorites and triggers filtering`() =
        runTest(testDispatcher) {
            // Set the favorite filter to true.
            viewModel.onEvent(CityEvent.OnFavoriteFilterChanged(true))
            // Also update search query so filtering is re-triggered.
            viewModel.onEvent(CityEvent.OnSearchQueryChanged("Test"))
            advanceTimeBy(400)
            // Verify that cityFilterUseCase is called with the updated favorites flag.
            verify { cityFilterUseCase("Test", true) }
        }

    @Test
    fun `onEvent OnCityTapped emits navigation event NavigateToMap`() = runTest(testDispatcher) {
        val emittedEvents = mutableListOf<NavigationEvent>()
        val job = launch {
            viewModel.navigationEvent.toList(emittedEvents)
        }
        val testCity = fakeCityModel().toUIItem()
        viewModel.onEvent(CityEvent.OnCityTapped(testCity))
        advanceTimeBy(100)
        // Check that the navigation event list contains the expected event.
        assertTrue(emittedEvents.contains(NavigationEvent.NavigateToMap(testCity)))
        job.cancel()
    }

    @Test
    fun `onEvent OnFavoriteToggled calls toggleFavoriteUseCase and refreshes search`() =
        runTest(testDispatcher) {
            // When
            viewModel.onEvent(CityEvent.OnFavoriteToggled(1L))
            advanceUntilIdle()  // Ensure the launched coroutine finishes
            // Then
            // Verify that toggleFavoriteUseCase was called with cityId 1.
            coVerify { toggleFavoriteUseCase(1L) }
            // Verify that cityFilterUseCase was called again (with any query, since we simply update the current query).
            verify { cityFilterUseCase(any(), any()) }
        }
}

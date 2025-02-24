package com.test.citychallenge.presentation.viewmodel

import androidx.paging.PagingData
import com.test.citychallenge.common.Response
import com.test.citychallenge.domain.usecase.CityFilterPaginationUseCase
import com.test.citychallenge.domain.usecase.GetSelectedCityUseCase
import com.test.citychallenge.domain.usecase.SetSelectedCityUseCase
import com.test.citychallenge.domain.usecase.SyncCitiesUseCase
import com.test.citychallenge.domain.usecase.ToggleFavoriteUseCase
import com.test.citychallenge.presentation.mapper.toModel
import com.test.citychallenge.presentation.model.CityUIItem
import com.test.citychallenge.presentation.navigation.Navigation
import com.test.citychallenge.presentation.navigation.NavigationManager
import io.mockk.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher

// Create a MainDispatcherRule to override Dispatchers.Main in tests
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    private val dispatcher: TestDispatcher = StandardTestDispatcher()
) : TestWatcher() {
    override fun starting(description: org.junit.runner.Description?) {
        Dispatchers.setMain(dispatcher)
    }
    override fun finished(description: org.junit.runner.Description?) {
        Dispatchers.resetMain()
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class CityViewModelTest {

    // Set up a test dispatcher and apply it as the Main dispatcher.
    private val testDispatcher = StandardTestDispatcher()

    @get:Rule
    var mainDispatcherRule = MainDispatcherRule(testDispatcher)

    // Mocks for dependencies
    private lateinit var syncCities: SyncCitiesUseCase
    private lateinit var toggleFavorite: ToggleFavoriteUseCase
    private lateinit var navigationManager: NavigationManager
    private lateinit var getSelectedCityUseCase: GetSelectedCityUseCase
    private lateinit var setSelectedCityUseCase: SetSelectedCityUseCase
    private lateinit var cityFilterPaginationUseCase: CityFilterPaginationUseCase

    // The ViewModel under test
    private lateinit var viewModel: CityViewModel

    @Before
    fun setup() {
        // Initialize mocks
        syncCities = mockk()
        toggleFavorite = mockk(relaxed = true)
        navigationManager = mockk(relaxed = true)
        getSelectedCityUseCase = mockk()
        setSelectedCityUseCase = mockk(relaxed = true)
        cityFilterPaginationUseCase = mockk()

        // By default, simulate a successful sync (which sets _isInitialLoading to false)
        coEvery { syncCities() } returns Response.Success(true)
        // By default, getSelectedCityUseCase returns no city
        coEvery { getSelectedCityUseCase() } returns Response.Success(null)
        // For paging, return an empty PagingData flow
        every { cityFilterPaginationUseCase(any(), any()) } returns flowOf(PagingData.empty())

        // Create the ViewModel. Note that the init { syncData() } block is automatically called.
        viewModel = CityViewModel(
            syncCities = syncCities,
            toggleFavorite = toggleFavorite,
            navigationManager = navigationManager,
            getSelectedCityUseCase = getSelectedCityUseCase,
            setSelectedCityUseCase = setSelectedCityUseCase,
            cityFilterPaginationUseCase = cityFilterPaginationUseCase
        )
    }

    @Test
    fun `syncData sets isInitialLoading to false on success`() = runTest {
        // Allow syncData (called in init) to complete.
        advanceUntilIdle()
        assertFalse(viewModel.isInitialLoading.value)
        coVerify { syncCities() }
    }

    @Test
    fun `onEvent OnSearchQueryChanged updates query and triggers pagination`() = runTest {
        val newQuery = "TestQuery"
        viewModel.onEvent(CityEvent.OnSearchQueryChanged(newQuery))
        // Advance time so that debounce and any async work completes.
        advanceTimeBy(350)
        advanceUntilIdle()
        // Collect one emission from the cities flow (which should trigger cityFilterPaginationUseCase)
        viewModel.cities.firstOrNull()
        // Verify that the paging use case was called with the new query and favorites flag false.
        verify { cityFilterPaginationUseCase(newQuery, false) }
    }

    @Test
    fun `onEvent OnCityTapped in landscape updates selected city and calls setSelectedCityUseCase`() = runTest {
        val dummyCity = dummyCityUIItem()
        viewModel.onEvent(CityEvent.OnCityTapped(dummyCity, isLandscape = true))
        advanceUntilIdle()
        // Verify that the selected city is updated
        assertEquals(dummyCity, viewModel.selectedCity.value)
        // Verify that the conversion and use case call occurred
        coVerify { setSelectedCityUseCase(dummyCity.toModel()) }
    }

    @Test
    fun `onEvent OnCityTapped not in landscape navigates to map`() = runTest {
        val dummyCity = dummyCityUIItem()
        viewModel.onEvent(CityEvent.OnCityTapped(dummyCity, isLandscape = false))
        advanceUntilIdle()
        // Verify that setSelectedCityUseCase was still called
        coVerify { setSelectedCityUseCase(dummyCity.toModel()) }
        // Verify that navigationManager.navigate was invoked with a Navigation.NavigateToMap command containing the city JSON.
        coVerify {
            navigationManager.navigate(match {
                it is Navigation.NavigateToMap && it.cityJson.contains(dummyCity.id.toString())
            })
        }
        // Since it's portrait, the selectedCity remains unchanged.
        assertNull(viewModel.selectedCity.value)
    }

    @Test
    fun `onEvent OnFavoriteToggled calls toggleFavorite and updates search query`() = runTest {
        val cityId = 42L
        viewModel.onEvent(CityEvent.OnFavoriteToggled(cityId))
        advanceUntilIdle()
        coVerify { toggleFavorite(cityId) }
    }

    @Test
    fun `onEvent OnFavoriteFilterChanged updates onlyFavorites state`() = runTest {
        // Set the favorite filter to true.
        viewModel.onEvent(CityEvent.OnFavoriteFilterChanged(true))
        // Then trigger a search query update.
        viewModel.onEvent(CityEvent.OnSearchQueryChanged("City"))
        advanceTimeBy(350)
        // Collect from the cities flow to trigger the combine block.
        viewModel.cities.firstOrNull()
        advanceUntilIdle()
        // Verify that the paging use case is called with the favorites flag true.
        verify { cityFilterPaginationUseCase("City", true) }
    }

    @Test
    fun `onEvent OnLoadInitialCity updates selectedCity with getSelectedCityUseCase result`() = runTest {
        val dummyCity = dummyCityUIItem()
        // Prepare getSelectedCityUseCase to return a success response with a city.
        coEvery { getSelectedCityUseCase() } returns Response.Success(dummyCity.toModel())
        viewModel.onEvent(CityEvent.OnLoadInitialCity)
        advanceUntilIdle()
        coVerify { getSelectedCityUseCase() }
        assertEquals(dummyCity, viewModel.selectedCity.value)
    }

    // Helper function to create a dummy CityUIItem.
    private fun dummyCityUIItem(): CityUIItem {
        return CityUIItem(
            id = 1L,
            name = "Dummy City",
            country = "TC",
            isFavorite = false,
            lat = 10.0,
            lon = 20.0
        )
    }
}

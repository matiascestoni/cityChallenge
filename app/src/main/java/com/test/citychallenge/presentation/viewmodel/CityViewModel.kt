package com.test.citychallenge.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.map
import com.test.citychallenge.common.Response
import com.test.citychallenge.domain.usecase.CityFilterPaginationUseCase
import com.test.citychallenge.domain.usecase.GetSelectedCityUseCase
import com.test.citychallenge.domain.usecase.SetSelectedCityUseCase
import com.test.citychallenge.domain.usecase.SyncCitiesUseCase
import com.test.citychallenge.domain.usecase.ToggleFavoriteUseCase
import com.test.citychallenge.presentation.mapper.toModel
import com.test.citychallenge.presentation.mapper.toUIItem
import com.test.citychallenge.presentation.model.CityUIItem
import com.test.citychallenge.presentation.navigation.Navigation
import com.test.citychallenge.presentation.navigation.NavigationManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

// Events from UI
sealed class CityEvent {
    data class OnSearchQueryChanged(val query: String) : CityEvent()
    data class OnCityTapped(val city: CityUIItem, val isLandscape: Boolean) : CityEvent()
    data class OnFavoriteToggled(val cityId: Long) : CityEvent()
    data class OnFavoriteFilterChanged(val enabled: Boolean) : CityEvent()
    data object OnLoadInitialCity : CityEvent()
}

@HiltViewModel
class CityViewModel @Inject constructor(
    private val syncCities: SyncCitiesUseCase,
    private val toggleFavorite: ToggleFavoriteUseCase,
    private val navigationManager: NavigationManager,
    private val getSelectedCityUseCase: GetSelectedCityUseCase,
    private val setSelectedCityUseCase: SetSelectedCityUseCase,
    private val cityFilterPaginationUseCase: CityFilterPaginationUseCase
) : ViewModel() {

    private val searchQuery = MutableStateFlow("")
    private val onlyFavorites = MutableStateFlow(false)

    private val _selectedCity = MutableStateFlow<CityUIItem?>(null)
    val selectedCity: StateFlow<CityUIItem?> = _selectedCity.asStateFlow()

    private val _isInitialLoading = MutableStateFlow(true)
    val isInitialLoading: StateFlow<Boolean> = _isInitialLoading

    init {
        syncData()
    }

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val cities: Flow<PagingData<CityUIItem>> = combine(
        searchQuery.debounce(300),
        onlyFavorites,
        flow { emitAll(isInitialLoading) }
    ) { query, favorites, isInitialLoading ->
        if (!isInitialLoading) { // Only start when initial load done
            cityFilterPaginationUseCase(query, favorites)
                .map { pagingData -> pagingData.map { it.toUIItem() } }
        } else {
            emptyFlow() // Don't emit until ready
        }
    }.flatMapLatest { it }

    fun onEvent(event: CityEvent) = viewModelScope.launch {
        when (event) {
            is CityEvent.OnSearchQueryChanged -> searchQuery.value = event.query
            is CityEvent.OnCityTapped -> {
                setSelectedCityUseCase(event.city.toModel())
                if (event.isLandscape) {
                    _selectedCity.value = event.city
                } else {
                    val cityJson = Json.encodeToString(event.city)
                    navigationManager.navigate(Navigation.NavigateToMap(cityJson))
                }
            }

            is CityEvent.OnFavoriteToggled -> {
                toggleFavorite(event.cityId)
                // Re-trigger search to reflect favorite changes
                searchQuery.update { it }
            }

            is CityEvent.OnFavoriteFilterChanged -> onlyFavorites.value = event.enabled
            CityEvent.OnLoadInitialCity -> _selectedCity.value = getSelectedCity()
        }
    }

    private suspend fun getSelectedCity(): CityUIItem? {
        return when (val result = getSelectedCityUseCase()) {
            is Response.Success -> result.result?.toUIItem()
            is Response.Error -> null
        }
    }

    private fun syncData() = viewModelScope.launch {
        when (syncCities()) {
            is Response.Success -> {
                _isInitialLoading.value = false
            }

            is Response.Error -> {
                _isInitialLoading.value = false
            }
        }
    }
}

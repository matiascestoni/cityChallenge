package com.test.citychallenge.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.test.citychallenge.common.Response
import com.test.citychallenge.domain.usecase.CityFilterUseCase
import com.test.citychallenge.domain.usecase.SyncCitiesUseCase
import com.test.citychallenge.domain.usecase.ToggleFavoriteUseCase
import com.test.citychallenge.presentation.mapper.toUIItem
import com.test.citychallenge.presentation.model.CityUIItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface UIState {
    data object Loading : UIState
    data class Success(val cities: List<CityUIItem>) : UIState
    data class Error(val message: String) : UIState
}

// Events from UI
sealed class CityEvent {
    data class OnSearchQueryChanged(val query: String) : CityEvent()
    data class OnCityTapped(val city: CityUIItem) : CityEvent()
    data class OnFavoriteToggled(val cityId: Long) : CityEvent()
    data class OnFavoriteFilterChanged(val enabled: Boolean) : CityEvent()
}

// Navigation events
sealed class NavigationEvent {
    data class NavigateToMap(val city: CityUIItem) : NavigationEvent()
}

@HiltViewModel
class CityViewModel @Inject constructor(
    private val syncCities: SyncCitiesUseCase,
    private val cityFilter: CityFilterUseCase,
    private val toggleFavorite: ToggleFavoriteUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<UIState>(UIState.Loading)
    val uiState: StateFlow<UIState> = _uiState

    private val _navigationEvent = MutableSharedFlow<NavigationEvent>()
    val navigationEvent: SharedFlow<NavigationEvent> = _navigationEvent.asSharedFlow()

    private val searchQuery = MutableStateFlow("")
    private val onlyFavorites = MutableStateFlow(false)

    init {
        syncData()
        observeSearch()
    }

    fun onEvent(event: CityEvent) {
        when (event) {
            is CityEvent.OnSearchQueryChanged -> {
                searchQuery.value = event.query
            }

            is CityEvent.OnCityTapped -> {
                viewModelScope.launch {
                    _navigationEvent.emit(NavigationEvent.NavigateToMap(event.city))
                }
            }

            is CityEvent.OnFavoriteToggled -> {
                viewModelScope.launch {
                    toggleFavorite(event.cityId)
                    // Re-trigger search to reflect favorite changes
                    searchQuery.update { it }
                }
            }

            is CityEvent.OnFavoriteFilterChanged -> {
                onlyFavorites.value = event.enabled
            }
        }
    }

    private fun syncData() = viewModelScope.launch {
        _uiState.value = UIState.Loading
        when (val result = syncCities()) {
            is Response.Success -> {
                // Force a search refresh to load synced data
                searchQuery.update { it } // Re-emits current query
            }

            is Response.Error -> {
                _uiState.value = UIState.Error(result.message)
            }
        }
    }

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    private fun observeSearch() = viewModelScope.launch {
        searchQuery
            .debounce(300) // Avoid rapid searches
            .combine(onlyFavorites) { query, favorites ->
                Pair(query, favorites)
            }
            .flatMapLatest { (query, favorites) ->
                cityFilter(query, favorites)
            }
            .collect { response ->
                when (response) {
                    is Response.Success -> {
                        _uiState.value = UIState.Success(response.result.map { it.toUIItem() })
                    }

                    is Response.Error -> {
                        _uiState.value = UIState.Error(response.message)
                    }
                }
            }
    }
}

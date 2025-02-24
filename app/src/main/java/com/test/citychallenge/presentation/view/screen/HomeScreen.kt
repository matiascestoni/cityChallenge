package com.test.citychallenge.presentation.view.screen

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.test.citychallenge.presentation.model.CityUIItem
import com.test.citychallenge.presentation.viewmodel.CityEvent
import com.test.citychallenge.presentation.viewmodel.CityViewModel

@Composable
fun HomeScreen(
    viewModel: CityViewModel = hiltViewModel(),
    modifier: Modifier = Modifier,
) {
    var currentQuery by rememberSaveable { mutableStateOf("") }
    var onlyFavorites by rememberSaveable { mutableStateOf(false) }
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    val cities = viewModel.cities.collectAsLazyPagingItems()
    val isInitialLoading by viewModel.isInitialLoading.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                OutlinedTextField(
                    value = currentQuery,
                    onValueChange = {
                        currentQuery = it
                        viewModel.onEvent(CityEvent.OnSearchQueryChanged(currentQuery))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    label = { Text("Search cities") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .testTag("favoritesCheckboxRow")
                ) {
                    Checkbox(
                        modifier = Modifier.testTag("favoritesCheckbox"),
                        checked = onlyFavorites,
                        onCheckedChange = {
                            onlyFavorites = it
                            viewModel.onEvent(CityEvent.OnFavoriteFilterChanged(it))
                        }
                    )
                    Text("Show favorites only", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            // Handle different loading states
            when (cities.loadState.refresh) {
                is LoadState.Loading -> {
                    FullScreenLoading()
                }

                is LoadState.Error -> {
                    val error = cities.loadState.refresh as LoadState.Error
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = error.error.message ?: "Error loading cities",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { cities.retry() }) {
                            Text("Retry")
                        }
                    }
                }

                else -> {}
            }
            if (isInitialLoading) {
                FullScreenLoading()
            } else {
                ShowCityList(cities, viewModel, isLandscape)
            }
        }
    }
}

@Composable
private fun ShowCityList(
    cities: LazyPagingItems<CityUIItem>,
    viewModel: CityViewModel,
    isLandscape: Boolean
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        items(
            count = cities.itemCount,
            key = { index -> cities[index]?.id ?: index }
        ) { index ->
            val city = cities[index]
            city?.let {
                CityListItem(
                    city = it,
                    onFavoriteClick = {
                        viewModel.onEvent(CityEvent.OnFavoriteToggled(it.id))
                    },
                    onClick = {
                        viewModel.onEvent(CityEvent.OnCityTapped(it, isLandscape))
                    }
                )
                if (index < cities.itemCount - 1) {
                    HorizontalDivider()
                }
            }
        }

        // Handle pagination loading
        when (cities.loadState.append) {
            is LoadState.Loading -> {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        FullScreenLoading()
                    }
                }
            }

            is LoadState.Error -> {
                item {
                    val error = cities.loadState.append as LoadState.Error
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = error.error.message ?: "Error loading more cities",
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { cities.retry() }) {
                            Text("Retry")
                        }
                    }
                }
            }

            else -> {}
        }

        // Empty state
        if (cities.itemCount == 0 && cities.loadState.refresh !is LoadState.Loading) {
            item {
                Text(
                    text = "No cities found",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}


@Composable
private fun CityListItem(
    city: CityUIItem,
    onFavoriteClick: () -> Unit,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "${city.name}, ${city.country}",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Lat: ${city.lat}, Lon: ${city.lon}",
                style = MaterialTheme.typography.bodySmall
            )
        }

        IconButton(
            onClick = onFavoriteClick,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                imageVector = if (city.isFavorite) {
                    Icons.Filled.Favorite
                } else {
                    Icons.Outlined.FavoriteBorder
                },
                tint = if (city.isFavorite) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                },
                contentDescription = "Toggle favorite"
            )
        }
    }
}

@Composable
private fun FullScreenLoading() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("LoadingIndicator"),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}
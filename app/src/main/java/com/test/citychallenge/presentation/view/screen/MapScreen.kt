package com.test.citychallenge.presentation.view.screen

import android.content.res.Configuration
import android.view.ViewGroup
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.test.citychallenge.presentation.model.CityUIItem
import com.test.citychallenge.presentation.viewmodel.CityEvent
import com.test.citychallenge.presentation.viewmodel.CityViewModel
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    navigationCity: CityUIItem?,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CityViewModel = hiltViewModel()
) {
    var showInfoText by rememberSaveable { mutableStateOf(false) }
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    val mapView = remember { mutableStateOf<MapView?>(null) }
    val selectedCity by viewModel.selectedCity.collectAsStateWithLifecycle()
    var city by remember { mutableStateOf<CityUIItem?>(null) }

    LaunchedEffect(selectedCity) {
        selectedCity?.let {
            city = it
            mapView.value?.let { view ->
                view.overlays.clear()
                val geoPoint = GeoPoint(it.lat, it.lon)
                view.controller.animateTo(geoPoint)
                view.controller.setZoom(6.0)

                val marker = Marker(view).apply {
                    position = geoPoint
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    title = it.name
                }
                view.overlays.add(marker)
            }
        }
    }


    // Update map when city changes
    LaunchedEffect(navigationCity) {
        viewModel.onEvent(CityEvent.OnLoadInitialCity)
        navigationCity?.let {
            city = it
            mapView.value?.let { view ->
                view.overlays.clear()
                val geoPoint = GeoPoint(it.lat, it.lon)
                view.controller.animateTo(geoPoint)
                view.controller.setZoom(6.0)

                val marker = Marker(view).apply {
                    position = geoPoint
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    title = it.name
                }
                view.overlays.add(marker)
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            if (!isLandscape) {
                TopAppBar(
                    title = { Text(city?.name ?: "") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = { showInfoText = !showInfoText }) {
                            Icon(Icons.Default.Info, "City Info")
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                OpenStreetMapView(
                    onMapReady = { map -> mapView.value = map }
                )
            }

            Text(
                text = "© OpenStreetMap contributors",
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier
                    .padding(8.dp)
            )

            ShowInfoIcon(showInfoText, city)
        }
    }
}


@Composable
private fun ColumnScope.ShowInfoIcon(
    showInfoText: Boolean,
    city: CityUIItem?
) {
    AnimatedVisibility(
        visible = showInfoText && city != null,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),

    ) {
        city?.let {
            CityInfo(it)
        }
    }
}

@Composable
fun CityInfo(city: CityUIItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            // City Name and Country
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = city.name,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Country",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = city.country,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Coordinates
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Coordinates",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Lat: ${city.lat}, Lon: ${city.lon}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Favorite Status
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Favorite",
                    modifier = Modifier.size(18.dp),
                    tint = if (city.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(
                        alpha = 0.6f
                    )
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (city.isFavorite) "Favorite" else "Not Favorite",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun OpenStreetMapView(
    onMapReady: (MapView) -> Unit = {}
) {
    AndroidView(
        factory = { ctx ->
            MapView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                setTileSource(TileSourceFactory.MAPNIK)
                minZoomLevel = 4.0
                maxZoomLevel = 19.0
                onMapReady(this)
            }
        },
        modifier = Modifier.fillMaxSize(),
        update = {
            // Initial setup handled by LaunchedEffect
        }
    )
}

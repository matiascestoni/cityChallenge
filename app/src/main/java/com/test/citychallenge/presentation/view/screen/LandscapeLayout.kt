package com.test.citychallenge.presentation.view.screen

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.test.citychallenge.presentation.viewmodel.CityEvent
import com.test.citychallenge.presentation.viewmodel.CityViewModel

@Composable
fun LandscapeLayout(viewModel: CityViewModel = hiltViewModel()) {

    val selectedCity by viewModel.selectedCity.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.onEvent(CityEvent.OnLoadInitialCity)
    }

    Row(modifier = Modifier.fillMaxSize()) {
        // Left Pane: City List (50% width)
        HomeScreen(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize()
        )

        // Right Pane: Map (50% width)
        MapScreen(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize(),
            navigationCity = selectedCity,
            onBack = {} // No back button in landscape
        )
    }
}
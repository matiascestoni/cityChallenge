package com.test.citychallenge.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.test.citychallenge.presentation.model.CityUIItem
import com.test.citychallenge.presentation.view.screen.HomeScreen
import com.test.citychallenge.presentation.view.screen.MapScreen
import kotlinx.serialization.json.Json

// Navigation events
sealed class Navigation {
    data class NavigateToMap(val cityJson: String) : Navigation()
}

@Composable
fun PortraitLayout(navigationManager: NavigationManager) {
    val navController = rememberNavController()

    LaunchedEffect(navigationManager.navigation) {
        navigationManager.navigation.collect { event ->
            when (event) {
                is Navigation.NavigateToMap -> navController.navigate(Map(event.cityJson))
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Home
    ) {
        composable<Home> {
            HomeScreen()
        }
        composable<Map> { backStackEntry ->
            val map = backStackEntry.toRoute<Map>()
            val city = Json.decodeFromString<CityUIItem>(map.cityJson)
            MapScreen(city, onBack = { navController.popBackStack() })
        }
    }
}
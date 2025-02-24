package com.test.citychallenge.presentation.view.screen

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import com.test.citychallenge.presentation.navigation.PortraitLayout
import com.test.citychallenge.presentation.navigation.NavigationManager

@Composable
fun MainApp(navigationManager: NavigationManager) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    if (isLandscape) {
        LandscapeLayout()
    } else {
        PortraitLayout(navigationManager)
    }
}
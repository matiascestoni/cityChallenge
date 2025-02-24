package com.test.citychallenge.presentation.navigation

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NavigationManager @Inject constructor() {
    private val _navigation = Channel<Navigation>((Channel.BUFFERED))
    val navigation = _navigation.receiveAsFlow()

    suspend fun navigate(navigation: Navigation) {
        _navigation.send(navigation)
    }
}